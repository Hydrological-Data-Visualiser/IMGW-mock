package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.edu.agh.imgwmock.model.DataType;
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.PolygonDataNew;
import pl.edu.agh.imgwmock.utils.PolygonsUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/polygonsV2")
public class PolygonV2Controller extends PolygonController implements DataController<PolygonDataNew> {
    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("PolygonsV2", "PolygonsV2", "Polygons copy for test purposes", DataType.POLYGON, "[mm]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    protected List<LocalDate> getAvailableDates() {
        List<LocalDate> polygons = PolygonsUtils.getPolygonData().stream()
                .map(PolygonDataNew::getDate)
                .map(a -> LocalDate.ofInstant(a, ZoneId.systemDefault()))
                .distinct().collect(Collectors.toList());
        return polygons;
    }
}
