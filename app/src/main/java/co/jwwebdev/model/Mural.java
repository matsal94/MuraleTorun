package co.jwwebdev.model;

import java.util.ArrayList;
import java.util.List;

public class Mural {

    private String name;
    private String address;
    private double lat;
    private double lon;
    private String description;
    private List<Integer> muralsImageList = new ArrayList<>();


    public Mural() {
    }


    public Mural(String name, String address, double lat, double lon, String description, List<Integer> muralsImageList) {

        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.muralsImageList = muralsImageList;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getMuralsImageList() {
        return muralsImageList;
    }

    public void setMuralsList(List<Integer> muralsImageList) {
        this.muralsImageList = muralsImageList;
    }
}
