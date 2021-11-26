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
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinkaV2")
public class KocinkaV2Controller implements DataController<PolylinePoint> {
    Logger logger = LoggerFactory.getLogger(KocinkaV2Controller.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("kocinkaV2", "Kocinka New Random", "New Random data from Kocinka", DataType.LINE, "[hPa]", "#32A852", "#E81CBF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<LocalDate> list = new ArrayList<>();
        for (int i = 1; i < 300; i++) {
            list.add(LocalDate.of(2020, Month.APRIL, 19).plusDays(i));
        }
        return list;
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylinePoint>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", instant);
        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    @Override
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom));
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylinePoint::getValue)).collect(Collectors.toList()).get(0).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    @Override
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom));
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylinePoint::getValue)).collect(Collectors.toList()).get(kocinka.size()-1).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request){
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom));
        List<Instant> timePointsAfter = kocinka.stream().map(PolylinePoint::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

        Instant instant;
        if(timePointsAfter.size() <= step) instant = timePointsAfter.get(timePointsAfter.size() - 1);
        else instant = timePointsAfter.get(step);

        return new ResponseEntity<>(instant, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    @Override
    public ResponseEntity getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request){
        List<PolylinePoint> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.empty());
        ArrayList<Instant> dayTimePoints = new ArrayList(Collections.singleton(kocinka.get(0).getDate()));
        return new ResponseEntity<>(dayTimePoints, HttpStatus.OK);
    }
}
