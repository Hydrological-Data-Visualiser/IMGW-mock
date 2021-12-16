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
import pl.edu.agh.imgwmock.model.*;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;
import pl.edu.agh.imgwmock.utils.NewKocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinka")
public class KocinkaController implements DataController<PolylineDataNew> {
    Logger logger = LoggerFactory.getLogger(KocinkaController.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("river", "Kocinka", "Random Kocinka data", DataType.LINE, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<LocalDate> list = new ArrayList<>();
        for (int i = 1; i < 30; i++) {
            list.add(LocalDate.of(2021, Month.AUGUST, i));
        }
        return list;
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolylineDataNew>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        logger.info("Getting Kocinka");
        List<PolylineDataNew> kocinka = NewKocinkaUtils.getNewKocinkaRandomData(instant);
        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    @Override
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylineDataOld> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom))
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylineDataOld::getValue)).collect(Collectors.toList()).get(0).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    @Override
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylineDataOld> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom))
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylineDataOld::getValue)).collect(Collectors.toList()).get(kocinka.size()-1).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request){
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolylineDataOld> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.of(instantFrom))
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        List<Instant> timePointsAfter = kocinka.stream().map(PolylineDataOld::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

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
        List<PolylineDataOld> kocinka = KocinkaUtils.getKocinka("src/main/resources/kocinka.csv", Optional.empty())
                .stream().filter(riverPoint -> riverPoint.getValue() != null).collect(Collectors.toList());
        ArrayList<Instant> dayTimePoints = new ArrayList(Collections.singleton(kocinka.get(0).getDate()));
        return new ResponseEntity<>(dayTimePoints, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Polyline>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        List<Polyline> stations = NewKocinkaUtils.getKocinkaStations();
        if (id.isPresent()) {
            Optional<Polyline> station = stations.stream().filter(station1 -> Objects.equals(station1.getId(), id.get())).findFirst();
            if (station.isPresent()) {
                return new ResponseEntity<>(List.of(station.get()), HttpStatus.OK);
            } else {
                //not found
                return new ResponseEntity<>(List.of(), HttpStatus.OK);
            }
        } else {
            // no id - get all stations from database
            return new ResponseEntity<>(stations, HttpStatus.OK);
        }
    }

}
