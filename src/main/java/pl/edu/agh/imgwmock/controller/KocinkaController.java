package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.agh.imgwmock.model.RiverPoint;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class KocinkaController {
    Logger logger = LoggerFactory.getLogger(IMGWStationsController.class);

    @CrossOrigin
    @GetMapping("/kocinka")
    public ResponseEntity<List<RiverPoint>> getKocinka(HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<RiverPoint> kocinka = CSVUtils.getKocinka("src/main/resources/kocinka.csv");
        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }
}
