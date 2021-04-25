package com.my.car.crawler.common;

import java.util.Iterator;

public interface CrawlerFeeder<E> {
    Iterator<E> iterator();
}
