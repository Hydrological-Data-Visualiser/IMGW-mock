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
        Info info = new Info("IMGW", DataType.POINTS);
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

        if (instant.isPresent()) {
            Instant ins = Instant.parse(instant.get());
            return new ResponseEntity<>(dailyPrecipitations.stream().filter(data -> data.getDate().equals(ins)).collect(Collectors.toList()), HttpStatus.OK);
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
}
