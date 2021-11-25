package pl.edu.agh.imgwmock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.imgwmock.model.DailyPrecipitation;
import pl.edu.agh.imgwmock.model.Info;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DataController<T> {
    ResponseEntity<Info> getInfo(HttpServletRequest request);

    ResponseEntity<List<T>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId,
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request
    );

    ResponseEntity<Double> getMinValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request);

    ResponseEntity<Double> getMaxValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request);
    
    ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request);

    ResponseEntity<List<Instant>> getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request
    );

}
