package pl.edu.agh.imgwmock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "stations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Station {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private int code;
    private double latitude;
    private double longitude;
}
