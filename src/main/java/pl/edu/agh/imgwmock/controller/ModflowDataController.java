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
import pl.edu.agh.imgwmock.model.PolygonDataNew;
import pl.edu.agh.imgwmock.model.PolygonDataOld;
import pl.edu.agh.imgwmock.model.Station;
import pl.edu.agh.imgwmock.utils.ModflowDataConverter;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Double> getMinValue(String instantFrom, int length, HttpServletRequest request) {
        return new ResponseEntity<>(converter.getMinValue(), HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/max")
    public ResponseEntity<Double> getMaxValue(String instantFrom, int length, HttpServletRequest request) {
        return new ResponseEntity<>(converter.getMaxValue(), HttpStatus.OK);
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

    @CrossOrigin
    @GetMapping("/stations")
    public ResponseEntity<List<Station>> getAllStations(
            @RequestParam(value = "id", required = false) Optional<Long> id,
            HttpServletRequest request
    ) {
        val list = converter.getStations();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/data")
    public ResponseEntity<List<PolygonDataNew>> getData(
            @RequestParam(value = "stationId", required = false) Optional<Long> stationId, // ignored
            @RequestParam(value = "date", required = false) Optional<String> dateString,
            @RequestParam(value = "dateFrom", required = false) Optional<String> dateFrom,
            @RequestParam(value = "dateTo", required = false) Optional<String> dateTo,
            @RequestParam(value = "dateInstant", required = false) Optional<String> instant,
            HttpServletRequest request) {
        return new ResponseEntity<>(converter.getData(), HttpStatus.OK);
    }
}
