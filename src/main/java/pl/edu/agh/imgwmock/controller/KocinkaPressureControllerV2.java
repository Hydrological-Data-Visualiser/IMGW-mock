package pl.edu.agh.imgwmock.controller;

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

@Controller
@RequestMapping("/kocinkaPressureV2")
public class KocinkaPressureControllerV2 extends KocinkaPressureController implements DataController {
    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverPressure", "Kocinka Points", "Kocinka Pressure data", DataType.POINTS, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}

