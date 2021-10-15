package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.repository.DailyPrecipitationRepository;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class IMGWDailyPrecipitationsController {
    private final DailyPrecipitationRepository dailyPrecipitationRepository;
    Logger logger = LoggerFactory.getLogger(IMGWDailyPrecipitationsController.class);

    public IMGWDailyPrecipitationsController(DailyPrecipitationRepository dailyPrecipitationRepository) {
        this.dailyPrecipitationRepository = dailyPrecipitationRepository;
    }

    @CrossOrigin
    @GetMapping("/precipitation/addPrecipitation")
    public ResponseEntity<String> addPrecipitation(HttpServletRequest request) {
        logger.info("Adding precipitations");
        dailyPrecipitationRepository.deleteAll();
        int added = 0;
        List<DailyPrecipitation> dailyPrecipitations = CSVUtils.getDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        for (DailyPrecipitation dailyPrecipitation : dailyPrecipitations) {
            dailyPrecipitationRepository.save(dailyPrecipitation);
            logger.info("Added " + ++added + "/" + dailyPrecipitations.size());
        }
        return new ResponseEntity<>("Added " + dailyPrecipitations.size() + " records", HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/precipitation")
    public ResponseEntity<List<DailyPrecipitation>> getDailyPrecipitationsById(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            HttpServletRequest request
    ) {
        logger.info("Getting precipitation data: stationId = " + stationId.toString() + " date = " + dateString.toString());
        Optional<LocalDate> date = Optional.empty();
        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = Optional.of(LocalDate.parse(dateString.get(), formatter));
        }

        if (stationId.isPresent() && date.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByStationIdAndDate(stationId.get(), date.get()), HttpStatus.OK);
        } else if (stationId.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByStationId(stationId.get()), HttpStatus.OK);
        } else if (date.isPresent()) {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findByDate(date.get()), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DailyPrecipitation>>(dailyPrecipitationRepository.findAll(), HttpStatus.OK);
        }
    }

    //returning 2-element list, because it is easier, no need to create another model class
    @CrossOrigin
    @GetMapping("/precipitation/{dateString}/maxmin")
    public ResponseEntity<List<Double>> getMaxMinDailyPrecipitationsInDay(
            @PathVariable String dateString,
            HttpServletRequest request
    ) {
        logger.info("Getting maxmin precipitation data: date = " + dateString);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);

        List<DailyPrecipitation> dailyPrecipitations = dailyPrecipitationRepository.findByDate(date);
        if (dailyPrecipitations.size() > 0) {
            List<Double> sorted = dailyPrecipitations.stream().map(DailyPrecipitation::getDailyPrecipitation).sorted().collect(Collectors.toList());
            Double minValue = sorted.get(0);
            Double maxValue = sorted.get(sorted.size() - 1);
            return new ResponseEntity<List<Double>>(List.of(minValue, maxValue), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<Double>>(List.of(), HttpStatus.OK);
        }
    }
}
