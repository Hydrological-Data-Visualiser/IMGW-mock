package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "dailyPrecipitation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPrecipitation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;
    private Long stationId;
    private String stationName;
    private Instant date;
    private Double value;
}
