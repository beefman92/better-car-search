package com.my.car.crawler.edmunds;

import com.my.car.crawler.common.CrawlerFeeder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class EdmundsOverviewPageFeeder implements CrawlerFeeder<Document> {
    private static final Logger logger = LogManager.getLogger(EdmundsOverviewPageFeeder.class);
    private final String baseUrl = "https://www.edmunds.com/";
    private final String pageUrl = "https://www.edmunds.com/used-all/";
    private int zip = 95123;
    private int radius;
    private int startPage;
    private int endPage;

    public EdmundsOverviewPageFeeder(int startPage, int endPage, int radius) {
        this.startPage = startPage;
        this.endPage = endPage;
        this.radius = radius;
    }

    public EdmundsOverviewPageFeeder(int startPage, int endPage) {
        this(startPage, endPage, 50);
    }

    @Override
    public Iterator<Document> iterator() {
        return new EdmundsOverviewPageIterator();
    }

    private class EdmundsOverviewPageIterator implements Iterator<Document> {
        private int index = startPage - 1;

        @Override
        public boolean hasNext() {
            return index + 1 < endPage;
        }

        @Override
        public Document next() {
            ++index;
            String url = String.format("%s?pagenumber=%d&radius=%d&zip=%d", pageUrl, index, radius, zip);
            try {
                Connection.Response response = Jsoup.connect(url).execute();
                logger.info("Get edmunds overview page({}) with url({})", index, url);
                try (FileWriter fileWriter = new FileWriter(
                        String.format("data/source/edmunds-used-p-%d-r-%d-z-%d.html", index, radius, zip))) {
                    fileWriter.write(response.body());
                }
                Document doc = response.parse();
                return doc;
            } catch (IOException e) {
                throw new RuntimeException(String.format("Cannot get document with url(%s).", url), e);
            }
        }
    }
}
