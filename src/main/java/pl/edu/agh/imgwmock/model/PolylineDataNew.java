package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class PolylineDataNew {
    private Long id;
    private Long polylineId;
    private Double value;
    private Instant date;
}
