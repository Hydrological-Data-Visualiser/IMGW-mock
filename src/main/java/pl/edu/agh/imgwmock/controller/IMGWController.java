package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.repository.DailyPrecipitationRepository;
import pl.edu.agh.imgwmock.repository.StationRepository;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
public class IMGWController {
    private final StationRepository stationRepository;
    private final DailyPrecipitationRepository dailyPrecipitationRepository;

    public IMGWController(StationRepository repository, DailyPrecipitationRepository dailyPrecipitationRepository) {
        this.stationRepository = repository;
        this.dailyPrecipitationRepository = dailyPrecipitationRepository;
    }

    @CrossOrigin
    @GetMapping("/addStations")
    public ResponseEntity<List<Station>> addStations(HttpServletRequest request) {
        stationRepository.deleteAll();
        List<Station> stations = CSVUtils.getStationListFromCSV("src/main/resources/wykaz_stacji.csv");
        stationRepository.saveAll(stations);
        return new ResponseEntity<List<Station>>(stations, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/addPrecipitation")
    public ResponseEntity<List<DailyPrecipitation>> addPrecipitation(HttpServletRequest request) {
        dailyPrecipitationRepository.deleteAll();
        List<DailyPrecipitation> dailyPrecipitations = CSVUtils.getDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        dailyPrecipitationRepository.saveAll(dailyPrecipitations);
        return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        if (id.isPresent()) {
            Optional<Station> station = stationRepository.findById(id.get());
            if (station.isPresent()) {
                return new ResponseEntity<List<Station>>(List.of(station.orElse(null)), HttpStatus.OK);
            } else {
                return new ResponseEntity<List<Station>>(List.of(), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<List<Station>>(stationRepository.findAll(), HttpStatus.OK);
        }
    }
}
