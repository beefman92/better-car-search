package com.my.car.search.query;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StructuredQueryTest {
    @Test
    public void testParsePriceFilter_withoutKeywordPrice_pass() {
        QueryParser queryParser = new QueryParser();
        StructuredQuery structuredQuery = new StructuredQuery();
        String query = "car less than $5,000.00";
        queryParser.parsePriceFilter(query, structuredQuery);
        List<Filter> filterList = structuredQuery.getFilterList();
        assertEquals(1, filterList.size());

        Filter filter = filterList.get(0);
        assertEquals("price", filter.getKey());
        assertEquals(Filter.Condition.LESS_THAN, filter.getCondition());
        assertEquals("5000.00", filter.getValue());
    }

    @Test
    public void testParsePriceFilter_withKeywordPrice_pass() {
        QueryParser queryParser = new QueryParser();
        StructuredQuery structuredQuery = new StructuredQuery();
        String query = "car price is between $5,000.00 and 10,000.50";
        queryParser.parsePriceFilter(query, structuredQuery);
        List<Filter> filterList = structuredQuery.getFilterList();
        assertEquals(2, filterList.size());

        Filter filter1 = filterList.get(0);
        assertEquals("price", filter1.getKey());
        assertEquals(Filter.Condition.GREATER_THAN, filter1.getCondition());
        assertEquals("5000.00", filter1.getValue());

        Filter filter2 = filterList.get(1);
        assertEquals("price", filter2.getKey());
        assertEquals(Filter.Condition.LESS_THAN, filter2.getCondition());
        assertEquals("10000.50", filter2.getValue());
    }

    @Test
    public void testParseMileageFilter_withoutKeywordMileage_pass() {
        QueryParser queryParser = new QueryParser();
        StructuredQuery structuredQuery = new StructuredQuery();
        String query = "car between 50,000 miles and 100,000.50";
        queryParser.parseMileageFilter(query, structuredQuery);
        List<Filter> filterList = structuredQuery.getFilterList();
        assertEquals(2, filterList.size());

        Filter filter1 = filterList.get(0);
        assertEquals("mileage", filter1.getKey());
        assertEquals(Filter.Condition.GREATER_THAN, filter1.getCondition());
        assertEquals("50000", filter1.getValue());

        Filter filter2 = filterList.get(1);
        assertEquals("mileage", filter2.getKey());
        assertEquals(Filter.Condition.LESS_THAN, filter2.getCondition());
        assertEquals("100000.50", filter2.getValue());
    }

    @Test
    public void testParseMileageFilter_withKeywordMileage_pass() {
        QueryParser queryParser = new QueryParser();
        StructuredQuery structuredQuery = new StructuredQuery();
        String query = "car mileage greater than 70,000 miles";
        queryParser.parseMileageFilter(query, structuredQuery);
        List<Filter> filterList = structuredQuery.getFilterList();
        assertEquals(1, filterList.size());

        Filter filter = filterList.get(0);
        assertEquals("mileage", filter.getKey());
        assertEquals(Filter.Condition.GREATER_THAN, filter.getCondition());
        assertEquals("70000", filter.getValue());
    }
}
