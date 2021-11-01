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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    public ResponseEntity<List<RiverPoint>> getKocinka(
            @RequestParam(value = "date", required = true) String dateString,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<DailyPrecipitation> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);

        kocinkaTemperatureData = kocinkaTemperatureData.stream()
                .filter(temperature -> temperature.getDate().equals(date)).collect(Collectors.toList());

        List<RiverPoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv");
        List<DailyPrecipitation> finalKocinkaTemperatureData = kocinkaTemperatureData;
        List<RiverPoint> result = new ArrayList<>();

        kocinka.forEach(point -> {
                    Station closestStation = findClosestStation(point);
                    List<DailyPrecipitation> values = finalKocinkaTemperatureData.stream()
                            .filter(temp -> temp.getDate().equals(date) && temp.getStationId().equals(closestStation.getId()))
                            .collect(Collectors.toList());
                    if (values.size() > 0) {
                        result.add(new RiverPoint(
                                point.getId(),
                                point.getLatitude(),
                                point.getLongitude(),
                                values.get(0).getDailyPrecipitation())
                        );
                    } else {
                        result.add(new RiverPoint(
                                point.getId(),
                                point.getLatitude(),
                                point.getLongitude(),
                                null)
                        );
                    }
                }
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private Station findClosestStation(RiverPoint point) {
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
