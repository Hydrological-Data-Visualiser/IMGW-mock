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
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.PolylinePoint;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/kocinka")
public class KocinkaController {
    Logger logger = LoggerFactory.getLogger(KocinkaController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("Kocinka", DataType.LINE);
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylinePoint>> getKocinka(
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv");
        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }
}
