package Modules;

public class
Coordinate {

    private double latitude;
    private double longitude;

    public Coordinate (double lat, double lon) {
        latitude = lat;
        longitude = lon;

    }

    public void setCoordinate (double lat, double lon) {
        this.longitude = lon;
        this.latitude = lat;
    }

    public double getLat() {
        return latitude;
    }

    public double getLon() {
        return longitude;
    }
}
