package pl.edu.agh.imgwmock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.imgwmock.model.Station;

public interface StationRepository extends JpaRepository<Station, Long> {
}
