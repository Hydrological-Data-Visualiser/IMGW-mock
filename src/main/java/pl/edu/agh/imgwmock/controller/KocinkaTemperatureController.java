package pl.edu.agh.imgwmock.controller;

import lombok.val;
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
import pl.edu.agh.imgwmock.utils.NewKocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/kocinkaTemperature")
public class KocinkaTemperatureController implements DataController<PolylineDataNew> {
    Logger logger = LoggerFactory.getLogger(KocinkaTemperatureController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverTemperature", "Kocinka Temperature", "Kocinka Temperature data", DataType.LINE, "[°C]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylineDataNew>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        List<PointData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
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

        if (stationId.isPresent()) {
            kocinkaTemperatureData = kocinkaTemperatureData.stream().filter(a -> a.getStationId().equals(stationId.get())).collect(Collectors.toList());
        }

        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt = dateFromOpt;
            kocinkaTemperatureData = kocinkaTemperatureData.stream().filter(a -> a.getDate().isAfter(finalDateFromOpt.get())).collect(Collectors.toList());
        }

        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            kocinkaTemperatureData = kocinkaTemperatureData.stream().filter(a -> a.getDate().isAfter(finalDateToOpt.get())).collect(Collectors.toList());
        }
        List<PolylineDataNew> result = new ArrayList<>();
        val kocinkaLines = NewKocinkaUtils.getKocinkaStations();
        Long lastId = 0L;
        for (Polyline point : kocinkaLines) {
            val closestStation = findClosestStation(point);
            val dataFromStation = kocinkaTemperatureData.stream()
                    .filter(a -> a.getStationId().equals(closestStation.getId())).collect(Collectors.toList()).get(0);
            result.add(new PolylineDataNew(
                    lastId,
                    point.getId(),
                    dataFromStation.getValue(),
                    dataFromStation.getDate()
            ));
            lastId += 1;
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private Point findClosestStation(Polyline point) {
        List<Point> kocinkaStations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
        AtomicReference<Double> smallestDistance = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Point> closestStation = new AtomicReference<>();

        kocinkaStations.forEach(station -> {
            Double distanceSquare = Math.abs(station.getPoints().get(0)[0]) - point.getPoints().get(0)[0] + Math.abs(station.getPoints().get(0)[1] - point.getPoints().get(0)[1]);
            if (distanceSquare < smallestDistance.get()) {
                smallestDistance.set(distanceSquare);
                closestStation.set(station);
            }
        });

        return closestStation.get();
    }

    private Stream<PointData> temperatureBetween(Instant instantFrom, Instant instantTo) {
        List<PointData> kocinka = KocinkaUtils.getKocinkaTemperatureData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        return kocinka.stream().filter(
                dailyPrecipitation -> {
                    Instant date = dailyPrecipitation.getDate();
                    return !date.isBefore(instantFrom) && !date.isAfter(instantTo);
                });
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<Double> getMinValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PointData> kocinka = KocinkaUtils.getKocinkaTemperatureData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(kocinka, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble minValue =
                temperatureBetween(dateFromInst, dateToInst).mapToDouble(PointData::getValue).min();

        if (minValue.isPresent())
            return new ResponseEntity<>(minValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<Double> getMaxValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PointData> kocinka = KocinkaUtils.getKocinkaTemperatureData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(kocinka, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble maxValue =
                temperatureBetween(dateFromInst, dateToInst).mapToDouble(PointData::getValue).max();

        if (maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<PointData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
        return kocinkaTemperatureData.stream().map(a -> LocalDate.ofInstant(a.getDate(), ZoneId.systemDefault())).distinct().collect(Collectors.toList());
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PointData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        List<Instant> timePointsAfter = getAggregatedTimePoints(kocinkaTemperatureData, dateFromInst, Instant.MAX);

        Instant instant;
        if (timePointsAfter.size() <= step) instant = timePointsAfter.get(timePointsAfter.size() - 1);
        else instant = timePointsAfter.get(step);

        return new ResponseEntity<>(instant, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    @Override
    public ResponseEntity getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Instant instantFrom = LocalDate.parse(dateString, formatter).atTime(0, 0, 0).toInstant(ZoneOffset.UTC);
        Instant instantTo = LocalDate.parse(dateString, formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<PointData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        List<Instant> ret = getAggregatedTimePoints(kocinkaTemperatureData, instantFrom, instantTo);

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    private List<Instant> getAggregatedTimePoints(List<PointData> list, Instant instantFrom, Instant instantTo) {
        List<Instant> dayTimePoints = list.stream().map(PointData::getDate).filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo)).sorted().distinct().collect(Collectors.toList());

        ArrayList<Instant> hours = new ArrayList<Instant>();
        for (Instant i : dayTimePoints) {
            if (!hours.contains(i)) {
                boolean canAdd = true;
                for (Instant j : hours) {
                    if (j.minusSeconds(900).isBefore(i) && j.plusSeconds(900).isAfter(i)) canAdd = false;
                }
                if (canAdd) hours.add(i);
            }
        }

        return hours.stream().sorted().distinct().collect(Collectors.toList());
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Polyline>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        List<Polyline> stations = NewKocinkaUtils.getKocinkaStations();
        if (id.isPresent()) {
            Optional<Polyline> station = stations.stream().filter(station1 -> Objects.equals(station1.getId(), id.get())).findFirst();
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
