package com.my.car.crawler.cargurus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.my.car.crawler.Utils;
import com.my.car.crawler.common.Crawler;
import com.my.car.entity.Car;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class CargurusCrawler implements Crawler {
    private static final Logger logger = LogManager.getLogger(CargurusCrawler.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    // zip, searchId, radius, offset, maxResults
    private String baseUrl = "https://www.cargurus.com/Cars/searchResults.action?zip=%d&inventorySearchWidgetType=AUTO&searchId=%s&nonShippableBaseline=0&sortDir=ASC&sourceContext=carGurusHomePageModel&distance=%d&sortType=DEAL_SCORE&offset=%d&maxResults=%d&filtersModified=true";
    // id, zip, radius
    private String detailPageUrl = "https://www.cargurus.com/Cars/detailListingJson.action?inventoryListing=%d&searchZip=%d&searchDistance=%d&inclusionType=undefined";
    private CargurusFeeder feeder;
    private HttpClient client;
    private final List<Car> carList;

    public CargurusCrawler(CargurusFeeder feeder) {
        carList = new ArrayList<>();
        this.feeder = feeder;
        client = new DefaultHttpClient();
    }

    @Override
    public void crawl() throws Exception {
        String uuid = UUID.randomUUID().toString();
        Iterator<CargurusFeeder.SearchCondition> iterator = feeder.iterator();
        while (iterator.hasNext()) {
            CargurusFeeder.SearchCondition condition = iterator.next();
            String url = String.format(
                    baseUrl, condition.zip, uuid, condition.radius, condition.offset, condition.maxResults);

            try {
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);

                try (BufferedReader reader = new BufferedReader
                        (new InputStreamReader(
                                response.getEntity().getContent()))) {
                    List<Object> list = gson.fromJson(reader, List.class);
                    String fileName = String.format(
                            "data/source/cargurus/overview/%d-%d-%d-%d.json",
                            condition.zip, condition.radius, condition.offset, condition.maxResults);
                    try (FileWriter writer = new FileWriter(fileName)) {
                        gson.toJson(list, writer);
                    } catch (IOException e) {
                        logger.warn("Failed to write overview to file({}): {}", fileName, e);
                    }

                    for (Object car: list) {
                        Map<String, Object> carMap = (Map<String, Object>) car;
                        int id = ((Number)carMap.get("id")).intValue();
                        if (id > 0) {
                            String carUrl = String.format(detailPageUrl, id, condition.zip, condition.radius);
                            HttpGet carGet = new HttpGet(carUrl);
                            try {
                                HttpResponse carResponse = client.execute(carGet);
                                try (BufferedReader carReader = new BufferedReader
                                        (new InputStreamReader(
                                                carResponse.getEntity().getContent()))) {
                                    StringBuilder builder = new StringBuilder();
                                    String line = "";
                                    while ((line = carReader.readLine()) != null) {
                                        builder.append(line);
                                    }
                                    String content = builder.toString();
                                    String carDetailFile = "data/source/cargurus/detail/" + id +".json";
                                    try (FileWriter carDetailWriter = new FileWriter(carDetailFile)) {
                                        carDetailWriter.write(content);
                                    } catch (Exception e) {
                                        logger.warn("Failed to write file for car({}): {}", id, e);
                                    }
                                    Map<String, Object> temp = (Map<String, Object>)gson.fromJson(content, Map.class);
                                    Map<String, Object> carDetail = (Map<String, Object>)temp.get("listing");
                                    Car carObject = convert(carDetail);
                                    if (StringUtils.isBlank(carObject.getVin())) {
                                        logger.warn("Cannot get vin for car({})", carObject.getTitle());
                                    } else {
                                        if (Utils.addVin(carObject.getVin())) {
                                            carList.add(carObject);
                                            logger.info("Get car({})", carObject.getVin());
                                        } else {
                                            logger.info("Discard duplicate car({})", carObject.getVin());
                                        }
                                    }

                                }
                            } catch (Exception e) {
                                logger.warn("Failed to get detail for car(" + id + ")", e);
                            }
                            long sleep = 5000 + Utils.getRandomSleepOffset(-2000, 2000);
                            logger.info("Sleep {} before next car.", sleep / 1000.0);
                            Thread.sleep(sleep);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to crawl page(" + url + ")", e);
            }
            long sleep = 30000 + Utils.getRandomSleepOffset(-15000, 15000);
            logger.info("Sleep {} before next page.", sleep / 1000.0);
            Thread.sleep(sleep);
        }

    }

    public Car convert(Map<String, Object> carDetail) {
        Car car = new Car();
        car.setTitle((String)carDetail.get("listingTitle"));
        car.setVin((String)carDetail.get("vin"));
        Object isCertified = carDetail.get("isCertified");
        if (isCertified != null) {
            car.setCertified((Boolean)isCertified);
        } else {
            car.setCertified(false);
        }


        car.setYear(((Number)carDetail.get("year")).intValue());
        car.setMake(((String)carDetail.get("makeName")).toLowerCase());
        car.setMode(((String)carDetail.get("modelName")).toLowerCase());
        car.setMileage(((Number)carDetail.get("mileage")).intValue());
        car.setPrice(((Number)carDetail.get("price")).intValue());

        Object description = carDetail.get("description");
        car.setDescription((String)description);
        List<String> optionList = (List<String>)carDetail.get("options");
        StringBuilder builder = new StringBuilder();
        if (optionList != null) {
            for (Object option: optionList) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append((String)option);
            }
        }
        car.setSpec(builder.toString());

        Object temp = carDetail.get("pictures");
        if (temp != null) {
            List<Map<String, Object>> pictureList = (List<Map<String, Object>>)(temp);
            if (pictureList.size() > 0) {
                Map<String, Object> pictureObject = pictureList.get(0);
                Object url = pictureObject.get("url");
                if (url != null) {
                    car.setImageUrl((String)url);
                }
            }
        }
        return car;
    }


    @Override
    public void save(String file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            gson.toJson(carList, fileWriter);
            logger.info("Write cars to file({})", file);
        }
    }
}
