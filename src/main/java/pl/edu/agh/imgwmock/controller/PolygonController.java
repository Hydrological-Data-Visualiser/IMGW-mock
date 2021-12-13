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
import pl.edu.agh.imgwmock.model.Polygon;
import pl.edu.agh.imgwmock.utils.CSVUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/polygons")
public class PolygonController implements DataController<Polygon> {
    Logger logger = LoggerFactory.getLogger(PolygonController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("Polygons", "Polygons", "Polygons random data", DataType.POLYGON, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    protected List<LocalDate> getAvailableDates() {
        List<LocalDate> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json").stream()
                .map(Polygon::getDate)
                .map(a -> LocalDate.ofInstant(a, ZoneId.systemDefault()))
                .distinct().collect(Collectors.toList());
        return polygons;
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<Polygon>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting example polygons");
        List<Polygon> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json");

        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();

        if (instant.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(instant.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(instant.get()).plusSeconds(900));
        }

        if (dateFromOpt.isPresent() && dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt = dateToOpt;
            Optional<Instant> finalDateFromOpt = dateFromOpt;

            polygons = polygons.stream().filter(polygon ->
                    polygon.getDate().isBefore(finalDateToOpt.get()) &&
                            polygon.getDate().isAfter(finalDateFromOpt.get())
            ).collect(Collectors.toList());
        }

        return new ResponseEntity<>(polygons, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    @Override
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        List<Polygon> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json")
                .stream().filter(polygon -> polygon.getValue() != null).collect(Collectors.toList());
        return new ResponseEntity<>(polygons.stream().sorted(Comparator.comparing(Polygon::getValue)).collect(Collectors.toList()).get(0).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    @Override
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        List<Polygon> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json")
                .stream().filter(polygon -> polygon.getValue() != null).collect(Collectors.toList());
        return new ResponseEntity<>(polygons.stream().sorted(Comparator.comparing(Polygon::getValue)).collect(Collectors.toList()).get(polygons.size() - 1).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<Polygon> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json")
                .stream().filter(polygon -> polygon.getValue() != null).collect(Collectors.toList());
        List<Instant> timePointsAfter = polygons.stream().map(Polygon::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

        Instant instant;
        if (timePointsAfter.size() <= step) instant = timePointsAfter.get(timePointsAfter.size() - 1);
        else instant = timePointsAfter.get(step);

        return new ResponseEntity<>(instant, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    @Override
    public ResponseEntity getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request) {
        List<Polygon> polygons = CSVUtils.getNewPolygons("src/main/resources/polygons.json")
                .stream().filter(polygon -> polygon.getValue() != null).collect(Collectors.toList());
        ArrayList<Instant> dayTimePoints = new ArrayList(Collections.singleton(polygons.get(0).getDate()));
        return new ResponseEntity<>(dayTimePoints, HttpStatus.OK);
    }
}
