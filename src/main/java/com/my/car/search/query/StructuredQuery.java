package com.my.car.search.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuredQuery {
    private Map<String, List<Filter>> filterMap;
    private String query;

    public StructuredQuery() {
        filterMap = new HashMap<>();
    }

    public Map<String, List<Filter>> getFilterMap() {
        return filterMap;
    }

    public void setFilterMap(Map<String, List<Filter>> filterMap) {
        this.filterMap = filterMap;
    }

    public void addFilter(Filter filter) {
        List<Filter> filterList = filterMap.computeIfAbsent(filter.getKey(), (key) -> new ArrayList<>());
        filterList.add(filter);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String[] generateFilterQuery() {
        List<String> filterQueryList = new ArrayList<>();
        for (Map.Entry<String, List<Filter>> entry: filterMap.entrySet()) {
            List<Filter> filterList = entry.getValue();
            if (filterList.size() == 1) {
                Filter filter = filterList.get(0);
                if (filter.getCondition() == Filter.Condition.EQUALS) {
                    filterQueryList.add(String.format("%s:%s", filter.getKey(), filter.getValue()));
                } else if (filter.getCondition() == Filter.Condition.LESS_THAN) {
                    filterQueryList.add(String.format("%s:[* TO %s]", filter.getKey(), filter.getValue()));
                } else {
                    filterQueryList.add(String.format("%s:[%s TO *]", filter.getKey(), filter.getValue()));
                }
            } else if (filterList.size() == 2) {
                // if these two conditions are valid, create a filter query.
                Filter filter1 = filterList.get(0);
                Filter filter2 = filterList.get(1);
                if (filter1.getCondition() == Filter.Condition.LESS_THAN && filter2.getCondition() == Filter.Condition.GREATER_THAN) {
                    filterQueryList.add(String.format("%s:[%s TO %s]", filter1.getKey(), filter2.getValue(), filter1.getValue()));
                } else if (filter1.getCondition() == Filter.Condition.GREATER_THAN && filter2.getCondition() == Filter.Condition.LESS_THAN) {
                    filterQueryList.add(String.format("%s:[%s TO %s]", filter1.getKey(), filter1.getValue(), filter2.getValue()));
                }
            }
        }
        return filterQueryList.toArray(new String[0]);
    }
}
