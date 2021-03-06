package com.my.car.crawler;

import com.my.car.crawler.cargurus.CargurusCrawler;
import com.my.car.crawler.cargurus.CargurusFeeder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.UUID;

public class CargurusCrawlerApp {
    private static final Logger logger = LogManager.getLogger(CargurusCrawlerApp.class);

    public static void main(String[] args) {
        if (args.length != 5) {
            throw new RuntimeException("Must provide start page, end page and results per page");
        }
        int zip = Integer.parseInt(args[0]);
        int radius = Integer.parseInt(args[1]);
        int startPage = Integer.parseInt(args[2]);
        int endPage = Integer.parseInt(args[3]);
        int resultsPerPage = Integer.parseInt(args[4]);
        logger.info("zip: {}, radius: {}, startPage: {}, endPage: {}, resultsPerPage: {}.",
                zip, radius, startPage, endPage, resultsPerPage);
        logger.info("Start crawling.");
        try {
            Utils.initVinSet();
            logger.info("Vin set loaded.");
        } catch (IOException e) {
            logger.error("Failed to initialize vin set: ", e);
            throw new RuntimeException(e);
        }
        CargurusFeeder feeder = new CargurusFeeder(zip, radius, startPage, endPage, resultsPerPage);
        CargurusCrawler crawler = new CargurusCrawler(feeder);

        try {
            crawler.crawl();
            crawler.save("data/cars/" + UUID.randomUUID() + ".json");
        } catch (Exception e) {
            logger.error("Failed to crawl: ", e);
        }

        try {
            Utils.saveVinSet();
        } catch (IOException e) {
            logger.error("Failed to store vin set: ", e);
            throw new RuntimeException(e);
        }
    }
}
