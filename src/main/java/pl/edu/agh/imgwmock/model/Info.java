package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Info {
    private String id;
    private String name;
    private String description;
    private DataType dataType;
    private String metricLabel;
    private String minColour;
    private String maxColour;
    private List<LocalDate> availableDates;
    // To be defined later

    public Info(String id, String name, String description, DataType dataType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.metricLabel = "";
        this.minColour = "#FFFFFF";
        this.maxColour = "#0000FF";
        this.availableDates = new ArrayList<>();
    }

    public Info(String name, DataType datatype) {
        this.id = name;
        this.name = name;
        this.description = "";
        this.dataType = datatype;
        this.metricLabel = "";
        this.minColour = "#FFFFFF";
        this.maxColour = "#0000FF";
        this.availableDates = new ArrayList<>();
    }

    public Info(String id, String name, String description, DataType dataType, String metricLabel, String minColour, String maxColour) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.metricLabel = metricLabel;
        this.minColour = minColour;
        this.maxColour = maxColour;
        this.availableDates = new ArrayList<>();
    }
}
