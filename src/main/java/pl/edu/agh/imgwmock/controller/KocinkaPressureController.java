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
import pl.edu.agh.imgwmock.utils.DailyPrecipitationUtils;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/kocinkaPressure")
public class KocinkaPressureController {
    Logger logger = LoggerFactory.getLogger(KocinkaPressureController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverPressure", "Kocinka Points", "Kocinka Pressure data", DataType.POINTS, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
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

    private Stream<DailyPrecipitation> pressureBetween(String instantFrom, int length) {
        List<DailyPrecipitation> kocinka = KocinkaUtils.getKocinkaPressureData();
        Instant dateFromInst = Instant.parse(instantFrom).minusSeconds(900);
        Instant dateToInst = DailyPrecipitationUtils.getInstantAfterDistinct(kocinka, dateFromInst, length);
        return kocinka.stream().filter(
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
                pressureBetween(instantFrom, length).mapToDouble(DailyPrecipitation::getValue).min();

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
                pressureBetween(instantFrom, length).mapToDouble(DailyPrecipitation::getValue).max();

        if (maxValue.isPresent())
            return new ResponseEntity<>(maxValue.getAsDouble(), HttpStatus.OK);
        else return new ResponseEntity<>(0.0, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<DailyPrecipitation> kocinka = KocinkaUtils.getKocinkaPressureData();
        return kocinka.stream().map(a -> LocalDate.ofInstant(a.getDate(), ZoneId.systemDefault())).distinct().collect(Collectors.toList());
    }
}
