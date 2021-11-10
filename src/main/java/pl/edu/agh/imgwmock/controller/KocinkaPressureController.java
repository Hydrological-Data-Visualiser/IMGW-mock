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
import pl.edu.agh.imgwmock.model.*;
import pl.edu.agh.imgwmock.utils.CSVUtils;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<DailyPrecipitation> kocinka = KocinkaUtils.getKocinkaPressureData();

        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();

        if (instant.isPresent()) {
            Instant ins = Instant.parse(instant.get());
            return new ResponseEntity<>(kocinka.stream().filter(data -> data.getDate().equals(ins)).collect(Collectors.toList()), HttpStatus.OK);
        }

        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateFromOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC));
            dateToOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC));

        }

        if (dateFrom.isPresent() && dateTo.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(dateFrom.get()));
            dateToOpt = Optional.of(Instant.parse(dateTo.get()));
        }


        if (dateFromOpt.isPresent() && dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            Optional<Instant> finalDateFromOpt = dateFromOpt;

            kocinka = kocinka.stream().filter(pressure ->
                    pressure.getDate().isBefore(finalDateToOpt.get()) &&
                            pressure.getDate().isAfter(finalDateFromOpt.get())
            ).collect(Collectors.toList());
        }

        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }
}
