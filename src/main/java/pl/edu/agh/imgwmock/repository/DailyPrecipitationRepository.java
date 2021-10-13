package pl.edu.agh.imgwmock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;

import java.time.LocalDate;
import java.util.List;

public interface DailyPrecipitationRepository extends JpaRepository<DailyPrecipitation, Long> {
    List<DailyPrecipitation> findByStationId(Long id);

    List<DailyPrecipitation> findByStationIdAndDate(Long id, LocalDate date);

    List<DailyPrecipitation> findByDate(LocalDate date);
}
