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
import pl.edu.agh.imgwmock.model.PolylineDataNew;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.NewKocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinka")
public class KocinkaController implements DataController<PolylineDataNew> {
    Logger logger = LoggerFactory.getLogger(KocinkaController.class);
    List<PolylineDataNew> kocinka = NewKocinkaUtils.getNewKocinkaRandomDataNewNew();

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("river", "Kocinka", "Random Kocinka data", DataType.LINE, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

//    private List<LocalDate> getAvailableDates() {
//        List<LocalDate> list = new ArrayList<>();
//        for (int i = 1; i < 30; i++) {
//            list.add(LocalDate.of(2021, Month.AUGUST, i));
//        }
//        return list;
//    }

    private List<LocalDate> getAvailableDates() {
        List<Instant> kocinka1 = this.kocinka.stream().map(PolylineDataNew::getDate).collect(Collectors.toList());
        List<LocalDate> list = new ArrayList<>();
        ZoneId zone = ZoneId.systemDefault();
        for (Instant instant : kocinka1) {
            list.add(LocalDate.ofInstant(instant, zone));
        }
        return list.stream().distinct().collect(Collectors.toList());
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
        List<PolylineDataNew> kocinka = this.kocinka;
        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();

        if (dateString.isPresent()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateFromOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC));
            dateToOpt = Optional.of(LocalDate.parse(dateString.get(), formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC));
        }

        if (dateFrom.isPresent() && dateTo.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(dateFrom.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(dateTo.get()).plusSeconds(900));
        }

        if (instant.isPresent()) {
            dateFromOpt = Optional.of(Instant.parse(instant.get()).minusSeconds(900));
            dateToOpt = Optional.of(Instant.parse(instant.get()).plusSeconds(900));
        }

        if (stationId.isPresent()) {
            kocinka = kocinka.stream().filter(precipitation -> precipitation.getPolylineId().equals(stationId.get())).collect(Collectors.toList());
        }
        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt1 = dateFromOpt;
            kocinka = kocinka.stream().filter(precipitation -> precipitation.getDate().isAfter(finalDateFromOpt1.get())).collect(Collectors.toList());
        }
        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt1 = dateToOpt;
            kocinka = kocinka.stream().filter(precipitation -> precipitation.getDate().isBefore(finalDateToOpt1.get())).collect(Collectors.toList());
        }

        return new ResponseEntity<>(kocinka, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    @Override
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylineDataNew> kocinka = this.kocinka
                .stream().filter(riverPoint -> riverPoint.getValue() != null)
                .filter(precipitation -> precipitation.getDate().isAfter(Instant.parse(instantFrom))).collect(Collectors.toList());
        if (kocinka.size() <= length) {
            kocinka = kocinka.subList(0, length - 1);
        }
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylineDataNew::getValue)).collect(Collectors.toList()).get(0).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    @Override
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        List<PolylineDataNew> kocinka = this.kocinka
                .stream().filter(riverPoint -> riverPoint.getValue() != null)
                .filter(precipitation -> precipitation.getDate().isAfter(Instant.parse(instantFrom))).collect(Collectors.toList());
        if (kocinka.size() <= length) {
            kocinka = kocinka.subList(0, length - 1);
        }
        return new ResponseEntity<>(kocinka.stream().sorted(Comparator.comparing(PolylineDataNew::getValue)).collect(Collectors.toList()).get(kocinka.size() - 1).getValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    @Override
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        Instant dateFromInst = Instant.parse(instantFrom);
        List<PolylineDataNew> kocinka = this.kocinka
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());
        List<Instant> timePointsAfter = kocinka.stream().map(PolylineDataNew::getDate).filter(date -> !date.isBefore(dateFromInst)).sorted().distinct().collect(Collectors.toList());

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Instant instantFrom = LocalDate.parse(dateString, formatter).atTime(0, 0, 0).minusSeconds(1).toInstant(ZoneOffset.UTC);
        Instant instantTo = LocalDate.parse(dateString, formatter).atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<PolylineDataNew> kocinka = this.kocinka
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());

        List<Instant> baseDayTimePoints = kocinka.stream().map(PolylineDataNew::getDate).filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo)).sorted().distinct().collect(Collectors.toList());

        return new ResponseEntity<>(baseDayTimePoints, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/length")
    @Override
    public ResponseEntity<Integer> getLengthBetween(
            @RequestParam(value = "instantFrom") String instantFromString,
            @RequestParam(value = "instantTo") String instantToString,
            HttpServletRequest request
    ) {
        Instant instantFrom = Instant.parse(instantFromString);
        Instant instantTo = Instant.parse(instantToString);

        List<PolylineDataNew> kocinka = this.kocinka
                .stream().filter(precipitation -> precipitation.getValue() != null).collect(Collectors.toList());

        int count =
                kocinka.stream()
                        .map(PolylineDataNew::getDate)
                        .filter(date -> !date.isBefore(instantFrom) && !date.isAfter(instantTo))
                        .sorted()
                        .distinct()
                        .collect(Collectors.toList()).size();

        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        List<Station> stations = NewKocinkaUtils.getKocinkaStations();
        if (id.isPresent()) {
            Optional<Station> station = stations.stream().filter(station1 -> Objects.equals(station1.getId(), id.get())).findFirst();
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
