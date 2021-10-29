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
import java.time.LocalDate;
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

//    @CrossOrigin
//    @GetMapping("/data/addPrecipitation")
//    public ResponseEntity<String> addPrecipitation(HttpServletRequest request) {
//        logger.info("Adding precipitations");
//        dailyPrecipitationRepository.deleteAll();
//        int added = 0;
//        List<DailyPrecipitation> dailyPrecipitations = ImgwUtils.getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
//        for (DailyPrecipitation dailyPrecipitation : dailyPrecipitations) {
//            dailyPrecipitationRepository.save(dailyPrecipitation);
//            logger.info("Added " + ++added + "/" + dailyPrecipitations.size());
//        }
//        return new ResponseEntity<>("Added " + dailyPrecipitations.size() + " records", HttpStatus.OK);
//    }

    @CrossOrigin
    @GetMapping("/data")
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

        List<DailyPrecipitation> dailyPrecipitations = ImgwUtils.getImgwDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        List<DailyPrecipitation> result;
        if (stationId.isPresent() && date.isPresent()) {
            Optional<LocalDate> finalDate = date;
            result = dailyPrecipitations.stream().filter(rain -> Objects.equals(rain.getStationId(), stationId.get()) && rain.getDate().equals(finalDate.get())).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (stationId.isPresent()) {
            result = dailyPrecipitations.stream().filter(rain -> Objects.equals(rain.getStationId(), stationId.get())).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (date.isPresent()) {
            Optional<LocalDate> finalDate = date;
            result = dailyPrecipitations.stream().filter(rain -> rain.getDate().equals(finalDate.get())).collect(Collectors.toList());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
        }
    }
}
