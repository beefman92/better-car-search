package com.my.car.search.query;

public class Filter {
    public enum Condition {
        EQUALS, GREATER_THAN, LESS_THAN
    }

    private String key;
    private Condition condition;
    private String value;

    public Filter(String key, Condition condition, String value) {
        this.key = key;
        this.condition = condition;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getValue() {
        return value;
    }
}
