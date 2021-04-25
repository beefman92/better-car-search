package com.my.car.crawler.cargurus;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class CargurusFeederTest {
    @Test
    public void testIterator_invalidPageRange_noLoop() {
        CargurusFeeder cargurusFeeder = new CargurusFeeder(10, 5, 50);
        Iterator<CargurusFeeder.SearchCondition> iterator = cargurusFeeder.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Assert.fail("Should not go into this loop");
            iterator.next();
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    public void testIterator_validPageRange_pass() {
        CargurusFeeder cargurusFeeder = new CargurusFeeder(0, 10, 100);
        Iterator<CargurusFeeder.SearchCondition> iterator = cargurusFeeder.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            CargurusFeeder.SearchCondition searchCondition = iterator.next();
            assertEquals(92801, searchCondition.zip);
            assertEquals(50, searchCondition.radius);
            assertEquals(count * 100, searchCondition.offset);
            assertEquals(100, searchCondition.maxResults);
            count++;
        }
        assertEquals(10, count);
    }
}
