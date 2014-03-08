package uk.ac.lancs.LUFELFv2.commsV2;

/**
 * Created by Luke on 08/03/14.
 */
public class Place {
    private String name;
    private String lat;
    private String lon;

    public double getLon() {
        return Double.parseDouble(lon);
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public double getLat() {
        return Double.parseDouble(lat);
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("name=" + name);
        b.append(",lat=" + lat.toString());
        b.append(",lon=" + lon.toString());

        return b.toString();
    }
}
