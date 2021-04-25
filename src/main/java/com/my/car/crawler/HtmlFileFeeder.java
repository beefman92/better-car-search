package com.my.car.crawler;

import com.my.car.crawler.common.CrawlerFeeder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class HtmlFileFeeder implements CrawlerFeeder<Document> {
    private List<String> fileList;
    private String baseUrl;

    public HtmlFileFeeder(List<String> fileList, String baseUrl) {
        this.fileList = fileList;
        this.baseUrl = baseUrl;
    }

    @Override
    public Iterator<Document> iterator() {
        return new HtmlFileIterator();
    }

    private class HtmlFileIterator implements Iterator<Document> {
        private int index;

        private HtmlFileIterator() {
            index = -1;
        }

        @Override
        public boolean hasNext() {
            return index + 1 < fileList.size();
        }

        @Override
        public Document next() {
            ++index;
            String filePath = fileList.get(index);
            try {
                return Jsoup.parse(new File(filePath), "utf-8", baseUrl);
            } catch (IOException e) {
                throw new NoSuchElementException(String.format("Parse html file(%s) failed: %s", filePath, e));
            }
        }
    }
}
