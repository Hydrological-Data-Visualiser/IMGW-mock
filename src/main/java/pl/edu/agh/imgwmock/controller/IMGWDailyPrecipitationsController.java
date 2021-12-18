package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.imgwmock.model.PointData;
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.DailyPrecipitationUtils;
import pl.edu.agh.imgwmock.utils.ImgwUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/imgw")
public class IMGWDailyPrecipitationsController implements DataController<PointData> {
    Logger logger = LoggerFactory.getLogger(IMGWDailyPrecipitationsController.class);

    public IMGWDailyPrecipitationsController() {
    }

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("IMGW", "IMGW", "Rain data from IMGW stations", DataType.POINTS, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PointData>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request
    ) {
        logger.info("Getting precipitation data: stationId = " + stationId.toString() + " date = " + dateString.toString());

        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();
        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull();

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
            dailyPrecipitations = dailyPrecipitations.stream().filter(precipitation -> precipitation.getStationId().equals(stationId.get())).collect(Collectors.toList());
        }
        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt1 = dateFromOpt;
            dailyPrecipitations = dailyPrecipitations.stream().filter(precipitation -> precipitation.getDate().isAfter(finalDateFromOpt1.get())).collect(Collectors.toList());
        }
        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt1 = dateToOpt;
            dailyPrecipitations = dailyPrecipitations.stream().filter(precipitation -> precipitation.getDate().isBefore(finalDateToOpt1.get())).collect(Collectors.toList());
        }
        return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
    }

    private Stream<PointData> precipitationBetween(String instantFrom, int length) {
        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull()
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());
        Instant dateFromInst = Instant.parse(instantFrom).minusSeconds(900);
        Instant dateToInst = DailyPrecipitationUtils.getInstantAfterDistinct(dailyPrecipitations, dateFromInst, length);
        return dailyPrecipitations.stream().filter(
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
                precipitationBetween(instantFrom, length).mapToDouble(PointData::getValue).min();

        if (minValue.isPresent())
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
                precipitationBetween(instantFrom, length).mapToDouble(PointData::getValue).max();

        if (maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull()
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());
        return dailyPrecipitations.stream().map(a -> LocalDate.ofInstant(a.getDate(), ZoneId.systemDefault())).distinct().collect(Collectors.toList());
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull()
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());
        List<Instant> timePointsAfter = dailyPrecipitations.stream().map(PointData::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

        Instant instant;
        if (timePointsAfter.size() <= step) instant = timePointsAfter.get(timePointsAfter.size() - 1);
        else instant = timePointsAfter.get(step);

        return new ResponseEntity<>(instant, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    @Override
    public ResponseEntity<List<Instant>> getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Instant instantFrom = LocalDate.parse(dateString, formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC);
        Instant instantTo = LocalDate.parse(dateString, formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull()
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());

        List<Instant> baseDayTimePoints = dailyPrecipitations.stream().map(PointData::getDate).filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo)).sorted().distinct().collect(Collectors.toList());

        return new ResponseEntity<>(baseDayTimePoints, HttpStatus.OK);
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

        List<PointData> dailyPrecipitations = ImgwUtils.getDailyPrecipitationsFromStationsWhereAllDataAreNotNull()
            .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());

        int count = 
          dailyPrecipitations.stream()
          .map(PointData::getDate)
          .filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo))
          .sorted()
          .distinct()
          .collect(Collectors.toList()).size();

        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        logger.info("Getting station data: stationId = " + id.toString());
        List<Station> stations = ImgwUtils.getStationsWhereAllDataAreNotNull();
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
