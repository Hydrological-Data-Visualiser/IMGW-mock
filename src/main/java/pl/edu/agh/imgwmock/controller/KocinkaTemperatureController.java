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
import pl.edu.agh.imgwmock.utils.DailyPrecipitationUtils;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/kocinkaTemperature")
public class KocinkaTemperatureController implements DataController<PolylinePoint> {
    Logger logger = LoggerFactory.getLogger(KocinkaTemperatureController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverTemperature", "Kocinka Temperature", "Kocinka Temperature data", DataType.LINE, "[°C]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylinePoint>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
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
            dateFromOpt = Optional.of(Instant.parse(instant.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(instant.get()).plusSeconds(900));
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

        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.empty());
        List<DailyPrecipitation> finalKocinkaTemperatureData = kocinkaTemperatureData;
        List<PolylinePoint> result = new ArrayList<>();

        kocinka.forEach(point -> {
                    Station closestStation = findClosestStation(point);
                    List<DailyPrecipitation> values = finalKocinkaTemperatureData.stream()
                            .filter(temp -> temp.getStationId().equals(closestStation.getId()))
                            .collect(Collectors.toList());
                    values.forEach(value ->
                            result.add(new PolylinePoint(
                                            point.getId(),
                                            point.getLatitude(),
                                            point.getLongitude(),
                                            value.getValue(),
                                            value.getDate()
                                    )
                            ));
//                    if (values.size() > 0) {
//                        result.add(new PolylinePoint(
//                                        point.getId(),
//                                        point.getLatitude(),
//                                        point.getLongitude(),
//                                        values.get(0).getValue(),
//                                        values.get(0).getDate()
//                                )
//                        );
//                    } else {
//                        result.add(new PolylinePoint(
//                                        point.getId(),
//                                        point.getLatitude(),
//                                        point.getLongitude(),
//                                        null,
//                                        point.getDate()
//                                )
//                        );
//                    }
                }
        );
        if (dateString.isPresent()) {
            return new ResponseEntity<>(result.stream().filter(distinctByKey(PolylinePoint::getDate)).collect(Collectors.toList()), HttpStatus.OK);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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

    private Stream<DailyPrecipitation> temperatureBetween(String instantFrom, int length) {
        List<DailyPrecipitation> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
        Instant dateFromInst = Instant.parse(instantFrom).minusSeconds(900);
        Instant dateToInst = DailyPrecipitationUtils.getInstantAfterDistinct(kocinkaTemperatureData, dateFromInst, length);
        return kocinkaTemperatureData.stream().filter(
                dailyPrecipitation -> {
                    Instant date = dailyPrecipitation.getDate();
                    return !date.isBefore(dateFromInst) && !date.isAfter(dateToInst);
                });
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<java.lang.Double> getMinValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        OptionalDouble minValue =
                temperatureBetween(instantFrom, length).mapToDouble(DailyPrecipitation::getValue).min();

        if(minValue.isPresent())
            return new ResponseEntity<>(minValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<java.lang.Double> getMaxValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        OptionalDouble maxValue =
                temperatureBetween(instantFrom, length).mapToDouble(DailyPrecipitation::getValue).max();

        if(maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<DailyPrecipitation> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
        return kocinkaTemperatureData.stream().map(a -> LocalDate.ofInstant(a.getDate(), ZoneId.systemDefault())).distinct().collect(Collectors.toList());
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request){
        Instant dateFromInst = Instant.parse(instantFrom);
        List<DailyPrecipitation> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
        List<Instant> timePointsAfter = kocinkaTemperatureData.stream().map(DailyPrecipitation::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

        Instant instant;
        if(timePointsAfter.size() <= step) instant = timePointsAfter.get(timePointsAfter.size() - 1);
        else instant = timePointsAfter.get(step);

        return new ResponseEntity<>(instant, HttpStatus.OK);
    }
}
