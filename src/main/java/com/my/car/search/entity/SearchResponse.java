package com.my.car.search.entity;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResponse {
    public static SearchResponse convert(QueryResponse queryResponse) {
        SearchResponse response = new SearchResponse();
        List<Document> documentList = new ArrayList<>();
        response.setTotal(queryResponse.getResults().getNumFound());
        Map<String, Map<String, List<String>>> highlightMap =  queryResponse.getHighlighting();
        if (highlightMap == null) {
            highlightMap = new HashMap<>();
        }
        for (SolrDocument solrDocument: queryResponse.getResults()) {
            Document document = new Document();
            document.vin = (String)solrDocument.getFieldValue("vin");
            document.title = (String)solrDocument.getFieldValue("title");
            document.certified = (Boolean)solrDocument.getFieldValue("certified");
            document.year = (Integer)solrDocument.getFieldValue("year");
            document.make = (String)solrDocument.getFieldValue("make");
            document.model = (String)solrDocument.getFieldValue("model");
            document.mileage = (Integer)solrDocument.getFieldValue("mileage");
            document.spec = (String)solrDocument.getFieldValue("spec");
            document.description = (String)solrDocument.getFieldValue("description");
            document.price = (Integer)solrDocument.getFieldValue("price");
            document.imageUrl = (String)solrDocument.getFieldValue("imageUrl");
            document.originPageLink = (String)solrDocument.getFieldValue("originPageLink");
            Map<String, List<String>> highlightContent = highlightMap.get(document.vin);
            if (highlightContent != null) {
                List<String> specList = highlightContent.get("spec");
                List<String> descriptionList = highlightContent.get("description");
                List<String> textHighlightList = highlightContent.get("_text_");
                if (specList != null) {
                    document.specHighlightList = specList;
                }
                if (descriptionList != null) {
                    document.descriptionHighlightList = descriptionList;
                }
                if (textHighlightList != null) {
                    document.textHighlightList = textHighlightList;
                }
            }
            documentList.add(document);
        }
        response.setDocumentList(documentList);

        if (queryResponse.getFacetRanges() != null) {
            for (org.apache.solr.client.solrj.response.RangeFacet rangeFacet : queryResponse.getFacetRanges()) {
                if (rangeFacet.getName().equals("price")) {
                    response.priceFacet = RangeFacet.convert(rangeFacet);
                } else if (rangeFacet.getName().equals("year")) {
                    response.yearFacet = RangeFacet.convert(rangeFacet);
                } else if (rangeFacet.getName().equals("mileage")) {
                    response.mileageFacet = RangeFacet.convert(rangeFacet);
                }
            }
        }

        response.fieldFacetList = new ArrayList<>();
        if (queryResponse.getFacetFields() != null) {
            for (org.apache.solr.client.solrj.response.FacetField facetField: queryResponse.getFacetFields()) {
                response.fieldFacetList.add(FieldFacet.convert(facetField));
            }
        }
        return response;
    }

    private long total;
    private List<Document> documentList;
    private RangeFacet priceFacet;
    private RangeFacet yearFacet;
    private RangeFacet mileageFacet;
    private List<FieldFacet> fieldFacetList;

    public List<FieldFacet> getFieldFacetList() {
        return fieldFacetList;
    }

    public void setFieldFacetList(List<FieldFacet> fieldFacetList) {
        this.fieldFacetList = fieldFacetList;
    }

    public List<Document> getDocumentList() {
        return documentList;
    }

    public void setDocumentList(List<Document> documentList) {
        this.documentList = documentList;
    }

    public RangeFacet getPriceFacet() {
        return priceFacet;
    }

    public void setPriceFacet(RangeFacet priceFacet) {
        this.priceFacet = priceFacet;
    }

    public RangeFacet getYearFacet() {
        return yearFacet;
    }

    public void setYearFacet(RangeFacet yearFacet) {
        this.yearFacet = yearFacet;
    }

    public RangeFacet getMileageFacet() {
        return mileageFacet;
    }

    public void setMileageFacet(RangeFacet mileageFacet) {
        this.mileageFacet = mileageFacet;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public static class FieldFacet {
        public static FieldFacet convert(org.apache.solr.client.solrj.response.FacetField solrFacet) {
            FieldFacet fieldFacet = new FieldFacet();
            fieldFacet.setName(solrFacet.getName());
            List<FieldCount> countList = new ArrayList<>();
            for (org.apache.solr.client.solrj.response.FacetField.Count solrCount: solrFacet.getValues()) {
                FieldCount fieldCount = new FieldCount();
                fieldCount.setName(solrFacet.getName());
                fieldCount.setKey(solrCount.getName());
                fieldCount.setCount(solrCount.getCount());
                fieldCount.generateId();
                countList.add(fieldCount);
            }
            fieldFacet.setCountList(countList);
            return fieldFacet;
        }

        private String name;
        private List<FieldCount> countList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<FieldCount> getCountList() {
            return countList;
        }

        public void setCountList(List<FieldCount> countList) {
            this.countList = countList;
        }

        static class FieldCount {
            private String id;
            private String name;
            private String key;
            private long count;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public long getCount() {
                return count;
            }

            public void setCount(long count) {
                this.count = count;
            }

            public void generateId() {
                id = name + "-" + key + "-" + count;
            }
        }
    }

    public static class RangeFacet {
        public static RangeFacet convert(
                org.apache.solr.client.solrj.response.RangeFacet<Integer, Integer> solrRangeFacet) {
            RangeFacet rangeFacet = new RangeFacet();
            rangeFacet.setName(solrRangeFacet.getName());
            rangeFacet.setStart(solrRangeFacet.getStart());
            rangeFacet.setEnd(solrRangeFacet.getEnd());
            rangeFacet.setGap(solrRangeFacet.getGap());

            List<Count> countList = new ArrayList<>();
            int before = (Integer)solrRangeFacet.getBefore();
            int after = (Integer)solrRangeFacet.getAfter();
            if (before != 0) {
                Count count = new Count(rangeFacet.getName(), String.valueOf(Integer.MIN_VALUE), Count.RangeType.LEFT_MOST, before, Integer.MIN_VALUE, solrRangeFacet.getStart());
                count.generateId();
                countList.add(count
                        );
            }
            int left = rangeFacet.getStart();
            int right = left + rangeFacet.getGap();
            for (org.apache.solr.client.solrj.response.RangeFacet.Count solrCount: solrRangeFacet.getCounts()) {
                if (solrCount.getCount() > 0) {
                    Count count = new Count();
                    count.setName(rangeFacet.getName());
                    count.setKey(solrCount.getValue());
                    count.setCount(solrCount.getCount());
                    count.setRangeType(Count.RangeType.MIDDLE);
                    count.setLeft(left);
                    count.setRight(right);
                    count.generateId();
                    countList.add(count);
                }
                left = right;
                right += rangeFacet.getGap();
            }
            if (after != 0) {
                Count count = new Count(rangeFacet.getName(), String.valueOf(solrRangeFacet.getEnd()), Count.RangeType.RIGHT_MOST, after, solrRangeFacet.getEnd(), Integer.MAX_VALUE);
                count.generateId();
                countList.add(count
                        );
            }
            if (countList.size() >= 2) {
                Count firstCount = countList.get(0);
                firstCount.setRangeType(Count.RangeType.LEFT_MOST);
                firstCount.setLeft(Integer.MIN_VALUE);
                firstCount.setKey(String.valueOf(Integer.MIN_VALUE));
                firstCount.setRight(countList.get(1).getLeft());
                firstCount.generateId();

                Count lastCount = countList.get(countList.size() - 1);
                lastCount.setRangeType(Count.RangeType.RIGHT_MOST);
                lastCount.setRight(Integer.MAX_VALUE);
                lastCount.setLeft(countList.get(countList.size() - 2).getRight());
                lastCount.setKey(String.valueOf(lastCount.getLeft()));
                lastCount.generateId();
            }

            rangeFacet.setCountList(countList);
            return rangeFacet;
        }

        private String name;
        private int start;
        private int end;
        private int gap;
        private List<Count> countList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getGap() {
            return gap;
        }

        public void setGap(int gap) {
            this.gap = gap;
        }

        public List<Count> getCountList() {
            return countList;
        }

        public void setCountList(List<Count> countList) {
            this.countList = countList;
        }

        static class Count {
            enum RangeType {
                LEFT_MOST, MIDDLE, RIGHT_MOST
            }
            private String id; // decide which is active
            private String name;  // field name
            private String key;
            RangeType rangeType;
            private int left;
            private int right;
            private int count;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void generateId() {
                this.id = name + "-" + left + "-" + rangeType;
            }

            public int getLeft() {
                return left;
            }

            public void setLeft(int left) {
                this.left = left;
            }

            public int getRight() {
                return right;
            }

            public void setRight(int right) {
                this.right = right;
            }

            public Count() {
            }

            public Count(String name, String key, RangeType rangeType, int count, int left, int right) {
                this.name = name;
                this.key = key;
                this.rangeType = rangeType;
                this.count = count;
                this.left = left;
                this.right = right;
            }

            public RangeType getRangeType() {
                return rangeType;
            }

            public void setRangeType(RangeType rangeType) {
                this.rangeType = rangeType;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }
        }
    }

    public static class Document {
        private String title = "";
        private boolean certified;
        private String vin;
        private int year;
        private String make;
        private String model;
        private int mileage;
        private String spec = "";
        private String description = "";
        private int price;
        private String imageUrl = "";
        private String originPageLink = "";

        private List<String> specHighlightList = new ArrayList<>();
        private List<String> descriptionHighlightList = new ArrayList<>();

        public List<String> getTextHighlightList() {
            return textHighlightList;
        }

        public void setTextHighlightList(List<String> textHighlightList) {
            this.textHighlightList = textHighlightList;
        }

        private List<String> textHighlightList = new ArrayList<>();

        public String getVin() {
            return vin;
        }

        public void setVin(String vin) {
            this.vin = vin;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getMileage() {
            return mileage;
        }

        public void setMileage(int mileage) {
            this.mileage = mileage;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public boolean isCertified() {
            return certified;
        }

        public void setCertified(boolean certified) {
            this.certified = certified;
        }

        public String getSpec() {
            return spec;
        }

        public void setSpec(String spec) {
            this.spec = spec;
        }

        public String getOriginPageLink() {
            return originPageLink;
        }

        public void setOriginPageLink(String originPageLink) {
            this.originPageLink = originPageLink;
        }

        public List<String> getSpecHighlightList() {
            return specHighlightList;
        }

        public void setSpecHighlightList(List<String> specHighlightList) {
            this.specHighlightList = specHighlightList;
        }

        public List<String> getDescriptionHighlightList() {
            return descriptionHighlightList;
        }

        public void setDescriptionHighlightList(List<String> descriptionHighlightList) {
            this.descriptionHighlightList = descriptionHighlightList;
        }
    }
}
