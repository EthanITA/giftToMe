package com.gifttome.gifttome;

import java.util.UUID;

public class AvailableObjectsData {
    private UUID id;
    private String issuer;
    private String category;
    private String name;
    private Double lat;
    private Double lon;
    private String description;
    private long twitterId;

    public  AvailableObjectsData(String name, String issuer, UUID id,
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getCategory() {
        return category;
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

    public Double getLon() {
        return lon;
    }

    public String getDescription() {
        return description;
    }
}