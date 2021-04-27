package com.my.car.crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.my.car.crawler.entity.Car;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DataImporter {
    private static final Logger logger = LogManager.getLogger(DataImporter.class);
    private static final Gson gson = new GsonBuilder().create();
    private HttpSolrClient httpSolrClient;
    private String directory;

    DataImporter(String directory) {
        httpSolrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/car").build();
        this.directory = directory;
    }

    public void importData() throws IOException, SolrServerException {
        File dir = new File(directory);
        File[] fileList = dir.listFiles();
        for (File dataFile: fileList) {
            long startTime = System.currentTimeMillis();
            logger.info("Import cars from file({}).", dataFile.getAbsolutePath());
            try (FileReader reader = new FileReader(dataFile)) {
                Car[] carArray = gson.fromJson(reader, Car[].class);

                for (Car car: carArray) {
                    SolrInputDocument document = convert(car);
                    httpSolrClient.add(document);
                }
                httpSolrClient.commit();
                float duration = (System.currentTimeMillis() - startTime) / 1000.f;
                logger.info("Importing {} cars from file({}) takes {}s.",
                        dataFile.getAbsolutePath(),
                        carArray.length,
                        duration);
            }
        }
    }

    private SolrInputDocument convert(Car car) {
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        solrInputDocument.addField("vin", car.getVin());
        solrInputDocument.addField("title", car.getTitle());
        solrInputDocument.addField("certified", car.isCertified());
        if (car.isCertified()) {
            solrInputDocument.addField("certifiedString", "certified");
        }
        solrInputDocument.addField("year", car.getYear());
        solrInputDocument.addField("make", car.getMake());
        solrInputDocument.addField("model", car.getMode());
        solrInputDocument.addField("price", car.getPrice());
        solrInputDocument.addField("mileage", car.getMileage());
        solrInputDocument.addField("spec", car.getSpec());
        solrInputDocument.addField("description", car.getDescription());
        solrInputDocument.addField("imageUrl", car.getImageUrl());
        solrInputDocument.addField("originPageLink", car.getOriginPageLink());
        return solrInputDocument;
    }

    public static void main(String[] args) throws Exception {
        String carDirectory = "./data/cars";
        logger.info("Start to import car data({}) into Solr", carDirectory);
        DataImporter importer = new DataImporter(carDirectory);
        importer.importData();
    }
}
