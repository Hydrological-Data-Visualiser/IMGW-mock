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
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.CSVUtils;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinkaPressure")
public class KocinkaPressureController {
    Logger logger = LoggerFactory.getLogger(KocinkaPressureController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("Kocinka Points", DataType.POINTS);
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getKocinkaStations(
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            HttpServletRequest request) {
        logger.info("Getting Kocinka Stations");
        List<Station> kocinkaStations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
        return new ResponseEntity<>(kocinkaStations, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<DailyPrecipitation>> getKocinka(
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");

        Optional<LocalDate> date = Optional.empty();
        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = Optional.of(LocalDate.parse(dateString.get(), formatter));
        }

        List<DailyPrecipitation> kocinka = KocinkaUtils.getKocinkaPressureData();
        if (date.isPresent()) {
            Optional<LocalDate> finalDate = date;
            kocinka = kocinka.stream().filter(pressure -> pressure.getDate() == finalDate.get()).collect(Collectors.toList());
        }
        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }
}
