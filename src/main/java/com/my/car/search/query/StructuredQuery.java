package com.my.car.search.query;

import java.util.ArrayList;
import java.util.List;

public class StructuredQuery {
    private List<Filter> filterList;
    private String query;

    public StructuredQuery() {
        filterList = new ArrayList<>();
    }

    public List<Filter> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<Filter> filterList) {
        this.filterList = filterList;
    }

    public void addFilter(Filter filter) {
        filterList.add(filter);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
