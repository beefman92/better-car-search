package com.my.car.crawler.edmunds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.my.car.crawler.Utils;
import com.my.car.crawler.common.Crawler;
import com.my.car.crawler.common.CrawlerFeeder;
import com.my.car.entity.Car;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdmundsOverviewPageCrawler implements Crawler {
    private static final Logger logger = LogManager.getLogger(EdmundsOverviewPageCrawler.class);
    private static final Pattern yearPattern = Pattern.compile("[0-9]{4}");
    private static final Pattern vinPattern = Pattern.compile("[\\w]{11,17}");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Car> carList;
    private CrawlerFeeder<Document> feeder;

    public EdmundsOverviewPageCrawler() {
        carList = new ArrayList<>();
    }

    public void setFeeder(CrawlerFeeder<Document> feeder) {
        this.feeder = feeder;
    }

    @Override
    public void crawl() throws Exception {
        Iterator<Document> iterator = feeder.iterator();
        while (iterator.hasNext()) {
            Document doc = null;
            try {
                doc = iterator.next();
            } catch (Exception e) {
                logger.warn("Cannot get document: ", e);
            }
            if (doc != null) {
                parsePage(doc);
            }
            if (iterator.hasNext()) {
                long sleep = 40000 + Utils.getRandomSleepOffset(-10000, 10000);
                logger.info("Sleep {} before next page.", sleep / 1000.0);
                Thread.sleep(sleep);
            }
        }
    }

    public void parsePage(Document document) {
        Elements elements = document.select(".d-flex.mb-0_75.mb-md-1_5.col-12.col-md-6");
        for (Element element: elements) {
            try {
                Car car = extract(element);
                if (StringUtils.isBlank(car.getVin())) {
                    logger.warn("Cannot get vin for car({})", car.getTitle());
                } else {
                    if (Utils.addVin(car.getVin())) {
                        carList.add(car);
                        logger.info("Get car({})", car.getVin());
                    } else {
                        logger.info("Discard duplicate car({})", car.getVin());
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to extract car", e);
            }
        }
    }

    public Car extract(Element card) throws Exception {
        Car car = new Car();

        // image
        Element imageElement = card.selectFirst("img");
        car.setImageUrl(imageElement.attr("src"));

        // mileage
        Element visibleInfoDiv = card.selectFirst("div.visible-vehicle-info");
        Element mileageSpan = visibleInfoDiv.selectFirst("div.row").selectFirst("span.size-14");
        String[] temp = mileageSpan.text().split(" ");
        if ("Not".equals(temp[0])) {
            car.setMileage(-1);
        } else {
            car.setMileage(NumberFormat.getNumberInstance(Locale.US).parse(temp[0]).intValue());
        }

        // price
        Element priceElement = card.selectFirst("span.display-price.font-weight-bold.text-gray-darker");
        int price = NumberFormat.getNumberInstance(Locale.US).parse(priceElement.text()).intValue();
        car.setPrice(price);

        // year
        String title = card.select("h2").select("a").text();
        Matcher matcher = yearPattern.matcher(title);
        if (matcher.find()) {
            car.setYear(Integer.parseInt(matcher.group()));
        }

        // title
        car.setTitle(title);

        // certified
        car.setCertified(title.toLowerCase().contains("certified"));

        // make and model
        int index = matcher.end() + 1;
        temp = title.substring(index).toLowerCase().trim().split(" ");
        car.setMake(temp[0]);
        if ("mini".equals(temp[0])) {
            car.setMode("cooper");
        } else {
            car.setMode(temp[1]);
        }

        // specs and vin
        Element infoDiv = card.selectFirst("div.vehicle-info.d-flex.flex-column.px-0_5.pl-1_25.pl-md-1.pl-lg-1_25.pb-0_5");
        Element hiddenInfoDiv = infoDiv.child(1);
        for (int i = 3; i < hiddenInfoDiv.childrenSize(); i++) {
            String text = hiddenInfoDiv.child(i).text();
            if ("Features and Specs:".equals(text)) {  // specs
                i++;
                car.setSpec(hiddenInfoDiv.child(i).text());
            } else if ("Listing Information:".equals(text)) {  // vin
                i++;
                String listInfo = hiddenInfoDiv.child(i).text();
                Matcher vinMatcher = vinPattern.matcher(listInfo);
                if (vinMatcher.find()) {
                    car.setVin(vinMatcher.group());
                } else {
                    car.setVin("");
                }
            }
        }

        // description
        Element descElement = hiddenInfoDiv.child(3);
        String descText = descElement.text();
        car.setDescription("");
        if (!"small font-weight-bold".equals(descElement.attr("class"))
                && !"section".equals(descElement.tagName())
                && !"AutoCheck Vehicle History Summary Unavailable.".equals(descText)) {
            car.setDescription(descText);
        }

        // origin page link;
        String link = card.selectFirst("a.usurp-inventory-card-vdp-link").attr("href");
        car.setOriginPageLink("https://www.edmunds.com" + link);
        return car;
    }

    @Override
    public void save(String file) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file)) {
            gson.toJson(carList, fileWriter);
        }
    }
}
