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
import pl.edu.agh.imgwmock.utils.NewKocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/kocinkaV2")
public class KocinkaV2Controller extends KocinkaController implements DataController<PolylineDataNew> {
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
