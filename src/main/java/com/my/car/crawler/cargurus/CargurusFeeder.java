package com.my.car.crawler.cargurus;

import com.my.car.crawler.common.CrawlerFeeder;

import java.util.Iterator;

public class CargurusFeeder implements CrawlerFeeder<CargurusFeeder.SearchCondition> {
    private final int zip;
    private final int radius;
    private final int startPage;
    private final int endPage;
    private final int maxResults;

    public CargurusFeeder(int startPage, int endPage, int maxResults) {
        this(92801, 50, startPage, endPage, maxResults);
    }

    public CargurusFeeder(int zip, int radius, int startPage, int endPage, int maxResults) {
        this.zip = zip;
        this.radius = radius;
        this.startPage = startPage;
        this.endPage = endPage;
        this.maxResults = maxResults;
    }

    @Override
    public Iterator<SearchCondition> iterator() {
        return new CargurusPageIterator();
    }

    public static class SearchCondition {
        final int zip;
        final int radius;
        final int offset;
        final int maxResults;

        public SearchCondition(int zip, int radius, int offset, int maxResults) {
            this.zip = zip;
            this.radius = radius;
            this.offset = offset;
            this.maxResults = maxResults;
        }
    }

    private class CargurusPageIterator implements Iterator<SearchCondition> {
        private int index = startPage - 1;

        @Override
        public boolean hasNext() {
            return index + 1 < endPage;
        }

        @Override
        public SearchCondition next() {
            ++index;
            return new SearchCondition(zip, radius, index * maxResults, maxResults);
        }
    }
}
