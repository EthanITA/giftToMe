package com.gifttome.gifttome;

public class AvailableObjectsData {
    private String id;
    private String issuer;
    private String category;
    private String name;
    private Integer lat;
    private Integer lon;
    private String description;
    private long twitterId;

    public  AvailableObjectsData(String name, String issuer){
        this.name = name;
        this.issuer = issuer;
        this.id = null;
        this.category = null;
        this.lat = null;
        this.lon = null;
        this.description = null;
       // this.twitterId = null;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
    }

    public AvailableObjectsData(String id, String issuer, String category, String name, Integer lat, Integer lon, String description) {
        this.id = id;
        this.issuer = issuer;
        this.category = category;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        //this.twitterId = null;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLat() {
        return lat;
    }

    public void setLat(Integer lat) {
        this.lat = lat;
    }

    public Integer getLon() {
        return lon;
    }

    public void setLon(Integer lon) {
        this.lon = lon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
