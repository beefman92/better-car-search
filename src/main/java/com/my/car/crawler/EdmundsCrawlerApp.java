package com.my.car.crawler;

import com.my.car.crawler.edmunds.EdmundsDetailPageCrawler;
import com.my.car.crawler.edmunds.EdmundsOverviewPageCrawler;
import com.my.car.crawler.edmunds.EdmundsOverviewPageFeeder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EdmundsCrawlerApp {
    private static final Logger logger = LogManager.getLogger(EdmundsCrawlerApp.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Must provide start page and end page");
        }
        int startPage = Integer.parseInt(args[0]);
        int endPage = Integer.parseInt(args[1]);
        logger.info("startPage: {}, endPage: {}.", startPage, endPage);
        logger.info("Start crawling.");
        try {
            Utils.initVinSet();
            logger.info("Vin set loaded.");
        } catch (IOException e) {
            logger.error("Failed to initialize vin set: ", e);
            throw new RuntimeException(e);
        }
        EdmundsOverviewPageCrawler edmundsOverviewPageCrawler = new EdmundsOverviewPageCrawler();
        EdmundsOverviewPageFeeder feeder = new EdmundsOverviewPageFeeder(startPage, endPage);
        try {
            edmundsOverviewPageCrawler.setFeeder(feeder);
            edmundsOverviewPageCrawler.crawl();
            edmundsOverviewPageCrawler.save("data/cars/" + UUID.randomUUID() + ".json");
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
