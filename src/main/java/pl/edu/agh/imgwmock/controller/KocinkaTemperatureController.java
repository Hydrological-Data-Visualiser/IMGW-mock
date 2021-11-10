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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinkaTemperature")
public class KocinkaTemperatureController {
    Logger logger = LoggerFactory.getLogger(KocinkaTemperatureController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("Kocinka Points", DataType.LINE);
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylinePoint>> getKocinka(
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<DailyPrecipitation> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();

        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();



        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateFromOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC));
            dateToOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC));
        }

        if (dateFrom.isPresent() && dateTo.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(dateFrom.get()));
            dateToOpt = Optional.of(Instant.parse(dateTo.get()));
        }

        if (instant.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(instant.get()).minusSeconds(1));
            dateToOpt = Optional.of(Instant.parse(instant.get()).plusSeconds(1));
        }

        Optional<Instant> finalDateFromOpt = dateFromOpt;
        Optional<Instant> finalDateToOpt = dateToOpt;

        if (dateFromOpt.isEmpty() || dateToOpt.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
        }

        kocinkaTemperatureData = kocinkaTemperatureData.stream()
                .filter(temperature ->
                        temperature.getDate().isAfter(finalDateFromOpt.get()) && temperature.getDate().isBefore(finalDateToOpt.get()))
                .collect(Collectors.toList());

        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv");
        List<DailyPrecipitation> finalKocinkaTemperatureData = kocinkaTemperatureData;
        List<PolylinePoint> result = new ArrayList<>();

        kocinka.forEach(point -> {
                    Station closestStation = findClosestStation(point);
                    List<DailyPrecipitation> values = finalKocinkaTemperatureData.stream()
                            .filter(temp -> temp.getStationId().equals(closestStation.getId()))
                            .collect(Collectors.toList());
                    if (values.size() > 0) {
                        result.add(new PolylinePoint(
                                        point.getId(),
                                        point.getLatitude(),
                                        point.getLongitude(),
                                        values.get(0).getValue(),
                                        values.get(0).getDate()
                                )
                        );
                    } else {
                        result.add(new PolylinePoint(
                                        point.getId(),
                                        point.getLatitude(),
                                        point.getLongitude(),
                                        null,
                                        point.getDate()
                                )
                        );
                    }
                }
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private Station findClosestStation(PolylinePoint point) {
        List<Station> kocinkaStations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
        AtomicReference<Double> smallestDistance = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Station> closestStation = new AtomicReference<>();

        kocinkaStations.forEach(station -> {
            Double distanceSquare = Math.abs(station.getLatitude() - point.getLatitude()) + Math.abs(station.getLongitude() - point.getLongitude());
            if (distanceSquare < smallestDistance.get()) {
                smallestDistance.set(distanceSquare);
                closestStation.set(station);
            }
        });

        return closestStation.get();
    }
}
