package com.gifttome.gifttome;

public class AvailableObjectsData {
    private String id;
    private String issuer;
    private String category;
    private String name;
    private Double lat;
    private Double lon;
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
    }
    public  AvailableObjectsData(double latitude, double longitude){
        this.name = "name";
        this.issuer = "issuer";
        this.id = null;
        this.category = null;
        this.lat = latitude;
        this.lon = longitude;
        this.description = "description";
    }
    public  AvailableObjectsData(String name, String issuer, String id,
                                 String category, double lat,
                                 double lon, String description){
        this.name = name;
        this.issuer = issuer;
        this.id = id;
        this.category = category;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
    }

    public long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(long twitterId) {
        this.twitterId = twitterId;
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
