package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.repository.DailyPrecipitationRepository;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
public class IMGWDailyPrecipitationsController {
    private final DailyPrecipitationRepository dailyPrecipitationRepository;

    public IMGWDailyPrecipitationsController(DailyPrecipitationRepository dailyPrecipitationRepository) {
        this.dailyPrecipitationRepository = dailyPrecipitationRepository;
    }

    @CrossOrigin
    @GetMapping("/precipitation/addPrecipitation")
    public ResponseEntity<List<DailyPrecipitation>> addPrecipitation(HttpServletRequest request) {
        dailyPrecipitationRepository.deleteAll();
        List<DailyPrecipitation> dailyPrecipitations = CSVUtils.getDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        dailyPrecipitationRepository.saveAll(dailyPrecipitations);
        return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/precipitation")
    public ResponseEntity<List<DailyPrecipitation>> getDailyPrecipitationsById(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            HttpServletRequest request
    ) {
        Optional<LocalDate> date = Optional.empty();
        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = Optional.of(LocalDate.parse(dateString.get(), formatter));
        }

        if (stationId.isPresent() && date.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByStationIdAndDate(stationId.get(), date.get()), HttpStatus.OK);
        } else if (stationId.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByStationId(stationId.get()), HttpStatus.OK);
        } else if (date.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByDate(date.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findAll(), HttpStatus.OK);
        }
    }
}
