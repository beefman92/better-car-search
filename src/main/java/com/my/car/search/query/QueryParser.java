package com.my.car.search.query;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    private static final Logger logger = LogManager.getLogger(QueryParser.class);
    private static final Set<String> makeSet = new HashSet<>();
    private static final Set<String> modelSet = new HashSet<>();
    private static final Pattern tokenPattern = Pattern.compile("[\\w,.$]+");
    private static final Pattern numberPattern = Pattern.compile("[0-9][0-9,.]+");
    private static final Pattern moneyPattern = Pattern.compile("\\$[0-9,.]+");
    private static final Pattern milePattern = Pattern.compile("[0-9,.]+\\s+mile");

    static {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("data/make.json")) {
            makeSet.addAll(gson.fromJson(reader, List.class));
            logger.info("Loaded car make data.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load car make data.", e);
        }
        try (FileReader reader = new FileReader("data/model.json")) {
            modelSet.addAll(gson.fromJson(reader, List.class));
            logger.info("Loaded car model data.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load car model data.", e);
        }
    }

    public StructuredQuery parse(String query) {
        String lowercase = query.toLowerCase(Locale.ROOT);
        List<Token> tokenList = new ArrayList<>();
        Matcher tokenMatcher = tokenPattern.matcher(lowercase);
        while (tokenMatcher.find()) {
            tokenList.add(new Token(tokenMatcher.group(), tokenMatcher.start()));
        }
        StructuredQuery structuredQuery = new StructuredQuery();

        // check make and model
        for (Token token: tokenList) {
            if (makeSet.contains(token.token)) {
                structuredQuery.addFilter(new Filter("make", Filter.Condition.EQUALS, token.token));
            } else if (modelSet.contains(token.token)) {
                structuredQuery.addFilter(new Filter("model", Filter.Condition.EQUALS, token.token));
            }
        }

        // check certified
        if (lowercase.contains("certified")) {
            structuredQuery.addFilter(new Filter("certified", Filter.Condition.EQUALS, "true"));
        }

        // check price
        parsePriceFilter(lowercase, structuredQuery);

        // check mileage
        parseMileageFilter(lowercase, structuredQuery);

        structuredQuery.setQuery(lowercase);
        return structuredQuery;
    }

    public void parseMileageFilter(String query, StructuredQuery structuredQuery) {
        int index = query.indexOf("mileage");
        if (index == -1) {
            Matcher mileMatcher = milePattern.matcher(query);
            if (mileMatcher.find()) {
                String mile = mileMatcher.group().split("\\s")[0];
                int mileIndex = mileMatcher.start();
                String substring = query.substring(0, mileIndex);
                if (substring.lastIndexOf("lower") != -1 || substring.lastIndexOf("less") != -1) {
                    structuredQuery.addFilter(new Filter("mileage", Filter.Condition.LESS_THAN, mile));
                } else if (substring.lastIndexOf("higher") != -1 || substring.lastIndexOf("greater") != -1) {
                    structuredQuery.addFilter(new Filter("mileage", Filter.Condition.GREATER_THAN, mile));
                } else if (substring.lastIndexOf("between") != -1) {
                    index = substring.lastIndexOf("between");
                    Matcher numberMatcher = numberPattern.matcher(query);
                    if (numberMatcher.find(index)) {
                        String number1 = numberMatcher.group().replaceAll("[,]", "");
                        index = numberMatcher.end();
                        if (numberMatcher.find(index)) {
                            String number2 = numberMatcher.group().replaceAll("[,]", "");
                            structuredQuery.addFilter(new Filter("mileage", Filter.Condition.GREATER_THAN, number1));
                            structuredQuery.addFilter(new Filter("mileage", Filter.Condition.LESS_THAN, number2));
                        }
                    }
                }
            }
        } else {
            List<Token> conditionTokenList = new ArrayList<>();
            conditionTokenList.add(new Token("lower", query.indexOf("lower", index)));
            conditionTokenList.add(new Token("less", query.indexOf("less", index)));
            conditionTokenList.add(new Token("higher", query.indexOf("higher", index)));
            conditionTokenList.add(new Token("greater", query.indexOf("greater", index)));
            conditionTokenList.add(new Token("between", query.indexOf("between", index)));
            Token targetToken = null;
            for (Token token: conditionTokenList) {
                if (token.index != -1 && (targetToken == null || (token.index < targetToken.index))) {
                    targetToken = token;
                }
            }
            if (targetToken != null) {
                Matcher numberMatcher = numberPattern.matcher(query);
                if (numberMatcher.find(targetToken.index)) {
                    int end = numberMatcher.end();
                    String number = numberMatcher.group().replaceAll("[,]", "");
                    if (targetToken.token.equals("lower") || targetToken.token.equals("less")) {
                        structuredQuery.addFilter(new Filter("mileage", Filter.Condition.LESS_THAN, number));
                    } else if (targetToken.token.equals("higher") || targetToken.token.equals("greater")) {
                        structuredQuery.addFilter(new Filter("mileage", Filter.Condition.GREATER_THAN, number));
                    } else {
                        if (numberMatcher.find(end)) {
                            String number2 = numberMatcher.group().replaceAll("[,]", "");
                            structuredQuery.addFilter(new Filter("mileage", Filter.Condition.GREATER_THAN, number));
                            structuredQuery.addFilter(new Filter("mileage", Filter.Condition.LESS_THAN, number2));
                        }
                    }
                }
            }
        }
    }

    public void parsePriceFilter(String query, StructuredQuery structuredQuery) {
        int index = query.indexOf("price");
        if (index == -1) {
            Matcher moneyMatcher = moneyPattern.matcher(query);
            if (moneyMatcher.find()) {
                String money = moneyMatcher.group().replaceAll("[$,]", "");
                int moneyIndex = moneyMatcher.start();
                String substring = query.substring(0, moneyIndex);
                if (substring.lastIndexOf("lower") != -1 || substring.lastIndexOf("less") != -1) {
                    structuredQuery.addFilter(new Filter("price", Filter.Condition.LESS_THAN, money));
                } else if (substring.lastIndexOf("higher") != -1 || substring.lastIndexOf("greater") != -1) {
                    structuredQuery.addFilter(new Filter("price", Filter.Condition.GREATER_THAN, money));
                } else if (substring.lastIndexOf("between") != -1) {
                    if (moneyMatcher.find()) {
                        String money2 = moneyMatcher.group().replaceAll("[$,]", "");
                        structuredQuery.addFilter(new Filter("price", Filter.Condition.GREATER_THAN, money));
                        structuredQuery.addFilter(new Filter("price", Filter.Condition.LESS_THAN, money2));
                    }
                }
            }
        } else {
            List<Token> conditionTokenList = new ArrayList<>();
            conditionTokenList.add(new Token("lower", query.indexOf("lower", index)));
            conditionTokenList.add(new Token("less", query.indexOf("less", index)));
            conditionTokenList.add(new Token("higher", query.indexOf("higher", index)));
            conditionTokenList.add(new Token("greater", query.indexOf("greater", index)));
            conditionTokenList.add(new Token("between", query.indexOf("between", index)));
            Token targetToken = null;
            for (Token token: conditionTokenList) {
                if (token.index != -1 && (targetToken == null || (token.index < targetToken.index))) {
                    targetToken = token;
                }
            }
            if (targetToken != null) {
                Matcher numberMatcher = numberPattern.matcher(query);
                if (numberMatcher.find(targetToken.index)) {
                    int end = numberMatcher.end();
                    String number = numberMatcher.group().replaceAll("[,]", "");
                    if (targetToken.token.equals("lower") || targetToken.token.equals("less")) {
                        structuredQuery.addFilter(new Filter("price", Filter.Condition.LESS_THAN, number));
                    } else if (targetToken.token.equals("higher") || targetToken.token.equals("greater")) {
                        structuredQuery.addFilter(new Filter("price", Filter.Condition.GREATER_THAN, number));
                    } else {
                        if (numberMatcher.find(end)) {
                            String number2 = numberMatcher.group().replaceAll("[,]", "");
                            structuredQuery.addFilter(new Filter("price", Filter.Condition.GREATER_THAN, number));
                            structuredQuery.addFilter(new Filter("price", Filter.Condition.LESS_THAN, number2));
                        }
                    }
                }
            }
        }
    }

    private static class Token {
        private String token;
        private int index;

        Token(String token, int index) {
            this.token = token;
            this.index = index;
        }
    }
}
