package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class Info {
    private String id;
    private String name;
    private String description;
    private DataType dataType;
    private String minColour;
    private String maxColour;
    // To be defined later

    public Info(String id, String name, String description, DataType dataType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.minColour = "#FFFFFF";
        this.maxColour = "#0000FF";
    }

    public Info(String name, DataType datatype) {
        this.id = name;
        this.name = name;
        this.description = "";
        this.dataType = datatype;
        this.minColour = "#FFFFFF";
        this.maxColour = "#0000FF";
    }
}
