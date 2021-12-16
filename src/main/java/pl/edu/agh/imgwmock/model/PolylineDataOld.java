package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class PolylineDataOld {
    private Long id;
    private Double latitude;
    private Double longitude;
    private Double value;
    private Instant date;
}
