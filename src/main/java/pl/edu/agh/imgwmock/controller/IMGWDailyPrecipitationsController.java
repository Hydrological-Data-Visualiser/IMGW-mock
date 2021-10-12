package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.repository.DailyPrecipitationRepository;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class IMGWDailyPrecipitationsController {
    private final DailyPrecipitationRepository dailyPrecipitationRepository;

    public IMGWDailyPrecipitationsController(DailyPrecipitationRepository dailyPrecipitationRepository) {
        this.dailyPrecipitationRepository = dailyPrecipitationRepository;
    }

    @CrossOrigin
    @GetMapping("/addPrecipitation")
    public ResponseEntity<List<DailyPrecipitation>> addPrecipitation(HttpServletRequest request) {
        dailyPrecipitationRepository.deleteAll();
        List<DailyPrecipitation> dailyPrecipitations = CSVUtils.getDailyPrecipitationListFromCSV("src/main/resources/o_d_08_2021.csv");
        dailyPrecipitationRepository.saveAll(dailyPrecipitations);
        return new ResponseEntity<>(dailyPrecipitations, HttpStatus.OK);
    }
}
