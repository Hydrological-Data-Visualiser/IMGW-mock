package pl.edu.agh.imgwmock.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.PointData;
import pl.edu.agh.imgwmock.model.PolygonDataNew;
import pl.edu.agh.imgwmock.model.PolygonDataOld;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.ImgwUtils;
import pl.edu.agh.imgwmock.utils.ModflowDataConverter;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/modflow")
public class ModflowDataController implements DataController<PolygonDataNew> {

    private final ModflowDataConverter converter = new ModflowDataConverter();

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = converter.getInfo();
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<Double> getMinValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        return new ResponseEntity<>(converter.getMinValueOnInterval(instantFrom, length), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<Double> getMaxValue(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "length") int length,
            HttpServletRequest request) {
        return new ResponseEntity<>(converter.getMaxValueOnInterval(instantFrom, length), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    public ResponseEntity<Instant> getTimePointAfter(
            @RequestParam(value = "instantFrom") String instantFrom,
            @RequestParam(value = "step") int step,
            HttpServletRequest request) {
        return new ResponseEntity<>(Instant.parse(converter.getTimePointAfter(instantFrom, step)), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    public ResponseEntity getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request) {
        return new ResponseEntity<>(List.of(converter.getData().get(0).getDate()), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/length")
    public ResponseEntity<Integer> getLengthBetween(
            @RequestParam(value = "instantFrom") String instantFromString,
            @RequestParam(value = "instantTo") String instantToString,
            HttpServletRequest request) {
        return new ResponseEntity<Integer>(converter.getLengthBetween(instantFromString, instantToString), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        val stations = converter.getStations();
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
        }}

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolygonDataNew>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {

        Optional<Instant> dateFromOpt = Optional.empty();
        Optional<Instant> dateToOpt = Optional.empty();
        List<PolygonDataNew> data = converter.getData();

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
            data = data.stream().filter(d -> d.getPolygonId().equals(stationId.get())).collect(Collectors.toList());
        }
        if (dateFromOpt.isPresent()) {
            Optional<Instant> finalDateFromOpt1 = dateFromOpt;
            data = data.stream().filter(d -> d.getDate().isAfter(finalDateFromOpt1.get())).collect(Collectors.toList());
        }
        if (dateToOpt.isPresent()) {
            Optional<Instant> finalDateToOpt1 = dateToOpt;
            data = data.stream().filter(d -> d.getDate().isBefore(finalDateToOpt1.get())).collect(Collectors.toList());
        }
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
