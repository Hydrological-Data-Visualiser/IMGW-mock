package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.imgwmock.model.*;
import pl.edu.agh.imgwmock.utils.PolygonsUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/polygonsV2")
public class PolygonV2Controller implements DataController<PolygonDataNew> {
    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("PolygonsV2", "PolygonsV2", "Polygons copy for test purposes", DataType.POLYGON, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    protected List<LocalDate> getAvailableDates() {
        List<LocalDate> polygons = PolygonsUtils.getPolygonData().stream()
                .map(PolygonDataNew::getDate)
                .map(a -> LocalDate.ofInstant(a, ZoneId.systemDefault()))
                .distinct().collect(Collectors.toList());
        return polygons;
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolygonDataNew>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();
        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData();

        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateFromOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC));
            dateToOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC));
        }

        if (dateFrom.isPresent() && dateTo.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(dateFrom.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(dateTo.get()).plusSeconds(900));
        }

        if (instant.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(instant.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(instant.get()).plusSeconds(900));
        }

        if (stationId.isPresent()) {
            polygons = polygons.stream().filter(precipitation -> precipitation.getPolygonId().equals(stationId.get())).collect(Collectors.toList());
        }
        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt1 = dateFromOpt;
            polygons = polygons.stream().filter(precipitation -> precipitation.getDate().isAfter(finalDateFromOpt1.get())).collect(Collectors.toList());
        }
        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt1 = dateToOpt;
            polygons = polygons.stream().filter(precipitation -> precipitation.getDate().isBefore(finalDateToOpt1.get())).collect(Collectors.toList());
        }
        return new ResponseEntity<>(polygons, HttpStatus.OK);
    }

    private Stream<PolygonDataNew> pressureBetween(Instant instantFrom, Instant instantTo) {
        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData()
                .stream().filter(polygon -> polygon.getValue() != null).collect(Collectors.toList());
        return polygons.stream().filter(
                dailyPrecipitation -> {
                    Instant date = dailyPrecipitation.getDate();
                    return !date.isBefore(instantFrom) && !date.isAfter(instantTo);
                });
    }

    private List<Instant> getAggregatedTimePoints(List<PolygonDataNew> list, Instant instantFrom, Instant instantTo) {
        List<Instant> dayTimePoints = list.stream().map(PolygonDataNew::getDate).filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo)).sorted().distinct().collect(Collectors.toList());

        ArrayList<Instant> hours = new ArrayList<Instant>();
        for (Instant i : dayTimePoints) {
            if (!hours.contains(i)) {
                boolean canAdd = true;
                for (Instant j : hours) {
                    if (j.minusSeconds(60 * 15).isBefore(i) && j.plusSeconds(60 * 15).isAfter(i)) canAdd = false;
                }
                if (canAdd) hours.add(i);
            }
        }

        return hours.stream().sorted().distinct().collect(Collectors.toList());
    }


    @CrossOrigin
    @GetMapping("/min")
    @Override
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(polygons, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble minValue =
                pressureBetween(dateFromInst, dateToInst).mapToDouble(PolygonDataNew::getValue).min();

        if (minValue.isPresent())
            return new ResponseEntity<>(minValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    @Override
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(polygons, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble minValue =
                pressureBetween(dateFromInst, dateToInst).mapToDouble(PolygonDataNew::getValue).max();

        if (minValue.isPresent())
            return new ResponseEntity<>(minValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        List<Instant> timePointsAfter = getAggregatedTimePoints(polygons, dateFromInst, Instant.MAX);

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

        List<PolygonDataNew> polygons = PolygonsUtils.getPolygonData()
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        List<Instant> ret = getAggregatedTimePoints(polygons, instantFrom, instantTo);

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Polygon>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        List<Polygon> stations = PolygonsUtils.getPolygonsStations();
        if (id.isPresent()) {
            Optional<Polygon> station = stations.stream().filter(station1 -> Objects.equals(station1.getId(), id.get())).findFirst();
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
