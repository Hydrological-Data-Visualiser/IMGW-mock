package pl.edu.agh.imgwmock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.HydrologicalData;
import pl.edu.agh.imgwmock.model.Info;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/kocinkaV2")
public class KocinkaV2Controller extends KocinkaController implements DataController {
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
}
