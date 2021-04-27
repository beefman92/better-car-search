package com.my.car.search.controller;

import com.my.car.search.entity.SearchResponse;
import com.my.car.search.query.QueryParser;
import com.my.car.search.query.StructuredQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class ApiController {
    private static final Logger logger = LogManager.getLogger(ApiController.class);
    private static final QueryParser queryParser = new QueryParser();

    private HttpSolrClient httpSolrClient;

    @PostConstruct
    public void init() {
        httpSolrClient = new HttpSolrClient.Builder("http://localhost:8983/solr/car").build();
    }

    @GetMapping("/greeting")
    public String greeting() {
        return "Hello";
    }

    @GetMapping("/api/v1")
    public String apiTest() {
        return "api is good.";
    }


    @CrossOrigin
    @GetMapping("/api/v1/search")
    public Object search(@RequestParam String query,
                                      @RequestParam(required = false) Integer offset,
                                      @RequestParam(required = false) Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null || limit == 0) {
            limit = 20;
        }
        logger.info("Search for query({}), offset({}), limit({}).", query, offset, limit);
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setStart(offset);
            solrQuery.setRows(limit);
            solrQuery.set("q", "_text_:" + query);
            solrQuery.setFields("*");
            StructuredQuery structuredQuery = queryParser.parse(query);
            solrQuery.setFilterQueries(structuredQuery.generateFilterQuery());

            // range facet
            solrQuery.set("facet", "true");
            solrQuery.addNumericRangeFacet("price", 10000, 50000, 10000);
            solrQuery.set("f.price.facet.range.other", "all");
            solrQuery.addNumericRangeFacet("year", 2010, 2021, 1);
            solrQuery.set("f.year.facet.range.other", "all");
            solrQuery.addNumericRangeFacet("mileage", 10000, 200000, 50000);
            solrQuery.set("f.mileage.facet.range.other", "all");

            // facet field
            solrQuery.addFacetField("make", "model", "certifiedString");
            solrQuery.set("f.make.facet.mincount", "1");
            solrQuery.set("f.model.facet.mincount", "1");
            solrQuery.set("f.certifiedString.facet.mincount", "1");

            // highlighter
            solrQuery.setHighlight(true);
            solrQuery.addHighlightField("spec");
            solrQuery.addHighlightField("description");

            QueryResponse response = httpSolrClient.query(solrQuery);
            return SearchResponse.convert(response);
        } catch (Exception e) {
            logger.error("Failed to search query.", e);
            throw new RuntimeException("Failed to search query: " + e.getMessage(), e);
        }
    }

    @CrossOrigin
    @GetMapping("/api/v1/search-facet")
    public SearchResponse searchFacet(@RequestParam String query,
                                           @RequestParam String facet,
                                           @RequestParam(required = false) Integer offset,
                                           @RequestParam(required = false) Integer limit) {
        if (offset == null) {
            offset = 0;
        }
        if (limit == null || limit == 0) {
            limit = 20;
        }
        logger.info("Search for query({}), offset({}), limit({}) with facet({}).", query, offset, limit, facet);
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setStart(offset);
            solrQuery.setRows(limit);
            solrQuery.set("q", "_text_:" + query);
            solrQuery.setFields("*");
            StructuredQuery structuredQuery = queryParser.parse(query);
            solrQuery.setFilterQueries(structuredQuery.generateFilterQuery());

            // Apply facet to filter query
            solrQuery.addFilterQuery(facet);

            // todo: highlighter
            QueryResponse response = httpSolrClient.query(solrQuery);
            return SearchResponse.convert(response);
        } catch (Exception e) {
            logger.error("Failed to search query with facet.", e);
            throw new RuntimeException("Failed to search query with facet: " + e.getMessage(), e);
        }
    }
}
