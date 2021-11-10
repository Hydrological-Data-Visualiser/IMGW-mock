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
    // To be defined later

    public Info(String name, DataType dataType) {
        this.name = name;
        this.description = "";
        this.dataType = dataType;
    }
}
