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
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.HydrologicalData;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.Station;
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
public class KocinkaTemperatureController implements DataController {
    Logger logger = LoggerFactory.getLogger(KocinkaTemperatureController.class);
    List<HydrologicalData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
    List<HydrologicalData> kocinkaTemperatureLineData = NewKocinkaUtils.getKocinkaTemperatureData();
    List<Station> stations = NewKocinkaUtils.getKocinkaStations();

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverTemperature", "Kocinka Temperature", "Kocinka Temperature data", DataType.LINE, "[°C]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<HydrologicalData>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        List<HydrologicalData> kocinkaTemperatureLineData = this.kocinkaTemperatureLineData;

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
            kocinkaTemperatureLineData = kocinkaTemperatureLineData.stream().filter(a -> a.getStationId().equals(stationId.get())).collect(Collectors.toList());
        }

        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt = dateFromOpt;
            kocinkaTemperatureLineData = kocinkaTemperatureLineData.stream().filter(a -> a.getDate().isAfter(finalDateFromOpt.get())).collect(Collectors.toList());
        }

        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            kocinkaTemperatureLineData = kocinkaTemperatureLineData.stream().filter(a -> a.getDate().isBefore(finalDateToOpt.get())).collect(Collectors.toList());
        }

        return new ResponseEntity<>(kocinkaTemperatureLineData, HttpStatus.OK);
    }

    private Station findClosestStation(Station point) {
        List<Station> kocinkaStations = CSVUtils.getStationListFromCSV("src/main/resources/kocinka/kocinka_stations.csv");
        AtomicReference<Double> smallestDistance = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Station> closestStation = new AtomicReference<>();

        kocinkaStations.forEach(station -> {
            Double distanceSquare = Math.abs(station.getPoints().get(0)[0] - point.getPoints().get(0)[0]) + Math.abs(station.getPoints().get(0)[1] - point.getPoints().get(0)[1]);
            if (distanceSquare < smallestDistance.get()) {
                smallestDistance.set(distanceSquare);
                closestStation.set(station);
            }
        });

        return closestStation.get();
    }

    private Stream<HydrologicalData> temperatureBetween(Instant instantFrom, Instant instantTo) {
        List<HydrologicalData> kocinka = this.kocinkaTemperatureData
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        return kocinka.stream().filter(
                dailyPrecipitation -> dailyPrecipitation.getDate().isAfter(instantFrom) && dailyPrecipitation.getDate().isBefore(instantTo));
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<Double> getMinValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<HydrologicalData> kocinka = this.kocinkaTemperatureData
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(kocinka, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble minValue =
                temperatureBetween(dateFromInst, dateToInst).mapToDouble(HydrologicalData::getValue).min();

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
        List<HydrologicalData> kocinka = this.kocinkaTemperatureData
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> aggregated = getAggregatedTimePoints(kocinka, dateFromInst, Instant.MAX);
        Instant dateToInst;
        if (aggregated.size() >= length) dateToInst = aggregated.get(length - 1);
        else dateToInst = aggregated.get(aggregated.size() - 1);
        dateToInst = dateToInst.plusSeconds(900);
        dateFromInst = dateFromInst.minusSeconds(900);

        OptionalDouble maxValue =
                temperatureBetween(dateFromInst, dateToInst).mapToDouble(HydrologicalData::getValue).max();

        if (maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<HydrologicalData> kocinkaTemperatureData = this.kocinkaTemperatureData;
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
        List<HydrologicalData> kocinkaTemperatureData = this.kocinkaTemperatureData
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

        List<HydrologicalData> kocinkaTemperatureData = this.kocinkaTemperatureData
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        List<Instant> ret = getAggregatedTimePoints(kocinkaTemperatureData, instantFrom, instantTo);

        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/length")
    @Override
    public ResponseEntity<Integer> getLengthBetween(
            @RequestParam(value = "instantFrom") String instantFromString,
            @RequestParam(value = "instantTo") String instantToString,
            HttpServletRequest request) {
        Instant instantFrom = Instant.parse(instantFromString);
        Instant instantTo = Instant.parse(instantToString);

        List<HydrologicalData> kocinkaTemperatureData = this.kocinkaTemperatureData
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());

        // if does not work properly, then divide by days and run getAggergatedTimePoints for each
        List<Instant> ret = getAggregatedTimePoints(kocinkaTemperatureData, instantFrom, instantTo);

        return new ResponseEntity<>(ret.size(), HttpStatus.OK);
    }

    private List<Instant> getAggregatedTimePoints(List<HydrologicalData> list, Instant instantFrom, Instant instantTo) {
        List<Instant> dayTimePoints = list.stream().map(HydrologicalData::getDate).filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo)).sorted().distinct().collect(Collectors.toList());

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
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        List<Station> stations = this.stations;
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
