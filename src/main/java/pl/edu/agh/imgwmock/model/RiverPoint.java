package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RiverPoint {
    private Long id;
    private double latitude;
    private double longitude;
    private double value;
}
