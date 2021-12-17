package pl.edu.agh.imgwmock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.PolygonDataOld;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.ModflowDataConverter;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/modflowTest")
public class ModflowDataControllerTest implements DataController<PolygonDataOld> {

    private final ModflowDataConverter converter = new ModflowDataConverter();

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = converter.getInfo();
        info.setId("modflowTest");
        info.setName("modflowTest");
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/min")
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        List<Double> data = converter.getData().stream().map(PolygonDataOld::getValue).filter(value -> value != -999.00).sorted().collect(Collectors.toList());
        return new ResponseEntity<>(data.get(0), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        List<Double> data = converter.getData().stream().map(PolygonDataOld::getValue).filter(value -> value != -999.00).sorted().collect(Collectors.toList());
        return new ResponseEntity<>(data.get(data.size() - 1), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/timePointsAfter")
    public ResponseEntity<Instant> getTimePointAfter(String instantFrom, int step, HttpServletRequest request) {
        return null;
    }

    @CrossOrigin
    @GetMapping("/dayTimePoints")
    public ResponseEntity getDayTimePoints(
            @RequestParam(value = "date") String dateString,
            HttpServletRequest request) {
        return new ResponseEntity<>(List.of(converter.getData().get(0).getDate()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Station>> getAllStations(Optional<Long> id, HttpServletRequest request) {
        return null;
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolygonDataOld>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
//        List<Polygon> data = converter.getData().stream().filter(a -> a.getValue() != -999.00).collect(Collectors.toList());
        List<PolygonDataOld> data = converter.getData().stream().peek(a ->{
            if (a.getValue() == -999.00) {
                a.setValue(null);
            }
        }).collect(Collectors.toList());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
