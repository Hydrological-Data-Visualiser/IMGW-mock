package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.repository.StationRepository;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/imgw")
public class IMGWStationsController {
    private final StationRepository stationRepository;
    Logger logger = LoggerFactory.getLogger(IMGWStationsController.class);

    public IMGWStationsController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

//    @CrossOrigin
//    @GetMapping("/stations/addAll")
//    public ResponseEntity<String> addStations(HttpServletRequest request) {
//        logger.info("Adding stations");
//        stationRepository.deleteAll();
//        int added = 0;
//        List<Station> stations = CSVUtils.getStationListFromCSV("src/main/resources/wykaz_stacji.csv");
//        for (Station station : stations) {
//            stationRepository.save(station);
//            logger.info("Added " + ++added + "/" + stations.size());
//        }
//        return new ResponseEntity<>("Added " + stations.size() + " records", HttpStatus.OK);
//    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        logger.info("Getting station data: stationId = " + id.toString());
        List<Station> stations = CSVUtils.getStationListFromCSV("src/main/resources/wykaz_stacji.csv");
        if (id.isPresent()) {
            Optional<Station> station = stations.stream().filter(station1 -> Objects.equals(station1.getId(), id.get())).findFirst();
            if (station.isPresent()) {
                return new ResponseEntity<>(List.of(station.get()), HttpStatus.OK);
            } else {
                //not found
                return new ResponseEntity<>(List.of(), HttpStatus.OK);
            }
        } else {
            // no id - get all stations from database
            return new ResponseEntity<>(stations, HttpStatus.OK);
        }
    }
}
