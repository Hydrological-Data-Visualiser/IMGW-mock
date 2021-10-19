package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.agh.imgwmock.model.Polygon;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class PolygonController {
    Logger logger = LoggerFactory.getLogger(PolygonController.class);

    @CrossOrigin
    @GetMapping("/polygons")
    public ResponseEntity<List<Polygon>> getPolygons(HttpServletRequest request) {
        logger.info("Getting example polygons");
        List<Polygon> polygons = CSVUtils.getPolygons("src/main/resources/polygons.json");
        return new ResponseEntity<>(polygons, HttpStatus.OK);
    }
}
