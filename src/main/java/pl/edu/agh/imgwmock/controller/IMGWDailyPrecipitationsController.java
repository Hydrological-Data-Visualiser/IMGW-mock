package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.repository.DailyPrecipitationRepository;
import pl.edu.agh.imgwmock.utils.ImgwUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/imgw")
public class IMGWDailyPrecipitationsController {
    private final DailyPrecipitationRepository dailyPrecipitationRepository;
    Logger logger = LoggerFactory.getLogger(IMGWDailyPrecipitationsController.class);

    public IMGWDailyPrecipitationsController(DailyPrecipitationRepository dailyPrecipitationRepository) {
        this.dailyPrecipitationRepository = dailyPrecipitationRepository;
    }

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("IMGW","IMGW","Rain data from IMGW stations", DataType.POINTS, "[mm]", "#FFF000", "#000FFF");
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<DailyPrecipitation>> getDailyPrecipitationsById(
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
        List<DailyPrecipitation> dailyPrecipitations = ImgwUtils.getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");

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

        List<DailyPrecipitation> result;
        if (stationId.isPresent() && dateToOpt.isPresent() && dateFromOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            Optional<Instant> finalDateFromOpt = dateFromOpt;
            result = dailyPrecipitations.stream().filter(rain ->
                    Objects.equals(rain.getStationId(), stationId.get()) &&
                            rain.getDate().isBefore(finalDateToOpt.get()) &&
                            rain.getDate().isAfter(finalDateFromOpt.get())).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (stationId.isPresent()) {
            result = dailyPrecipitations.stream().filter(rain -> Objects.equals(rain.getStationId(), stationId.get())).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (dateToOpt.isPresent() && dateFromOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            Optional<Instant> finalDateFromOpt = dateFromOpt;
            result = dailyPrecipitations.stream().filter(rain ->
                    rain.getDate().isAfter(finalDateFromOpt.get()) &&
                            rain.getDate().isBefore(finalDateToOpt.get())
            ).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
        }
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<java.lang.Double> getMinValue(HttpServletRequest request) {
        List<DailyPrecipitation> dailyPrecipitations = ImgwUtils.getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        OptionalDouble minValue = dailyPrecipitations.stream().mapToDouble(DailyPrecipitation::getValue).min();
        if(minValue.isPresent())
            return new ResponseEntity<>(minValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<java.lang.Double> getMaxValue(HttpServletRequest request) {
        List<DailyPrecipitation> dailyPrecipitations = ImgwUtils.getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        OptionalDouble maxValue = dailyPrecipitations.stream().mapToDouble(DailyPrecipitation::getValue).max();
        if(maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }
}
