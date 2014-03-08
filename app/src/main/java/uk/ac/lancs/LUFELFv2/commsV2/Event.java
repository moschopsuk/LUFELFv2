package uk.ac.lancs.LUFELFv2.commsV2;

import java.util.ArrayList;

/**
 * Created by Luke on 06/03/14.
 */
public class Event {
    private String name;
    private String date;
    private String type;
    private String description;
    private String locationName;
    private String locationAddress;
    private String location;
    private String username;
    private String email;
    private ArrayList<String> attendees;

    public Event() {
        this.attendees = new ArrayList<String>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addAttendee(String name) {
        this.attendees.add(name);
    }

    public ArrayList<String>  getAttendees() {
        return this.attendees;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("name=" + name);
        b.append(",date=" + date);
        b.append(",type=" + type);
        b.append(",description=" + description);
        b.append(",locationName=" + locationName);
        b.append(",locationAddress=" + locationAddress);
        b.append(",location=" + location);
        b.append(",username=" + username);
        b.append(",email=" + email);

        b.append(",attendees=");
        for (String temp : attendees) {
            b.append(temp + ",");
        }

        return b.toString();
    }
}
