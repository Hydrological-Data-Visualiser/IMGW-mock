package pl.edu.agh.imgwmock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;

public interface DailyPrecipitationRepository extends JpaRepository<DailyPrecipitation, Long> {
}
