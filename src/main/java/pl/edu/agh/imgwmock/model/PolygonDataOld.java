package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PolygonDataOld {
    private Long id;
    private List<Double[]> points;
    private Double value;
    private Instant date;
}
