package com.my.car.crawler.common;

import java.io.IOException;

public interface Crawler {
    void crawl() throws Exception;

    void save(String file) throws IOException;
}
