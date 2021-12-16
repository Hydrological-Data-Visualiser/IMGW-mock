package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Polyline implements Station {
    private Long id;
    private String name;
    private List<Double[]> points;
}
