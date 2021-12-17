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
import pl.edu.agh.imgwmock.model.Info;
import pl.edu.agh.imgwmock.model.PointData;
import pl.edu.agh.imgwmock.model.PolylineDataNew;
import pl.edu.agh.imgwmock.utils.KocinkaUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/kocinkaTemperatureV2")
public class KocinkaTemperatureV2Controller extends KocinkaTemperatureController implements DataController<PolylineDataNew> {
    Logger logger = LoggerFactory.getLogger(KocinkaTemperatureV2Controller.class);

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = new Info("riverTemperature1", "Kocinka Temperature new", "Kocinka Temperature data", DataType.LINE, "[°C]", "#FFF000", "#000FFF", getAvailableDates());
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    private List<LocalDate> getAvailableDates() {
        List<PointData> kocinkaTemperatureData = KocinkaUtils.getKocinkaTemperatureData();
        return kocinkaTemperatureData.stream().map(a -> LocalDate.ofInstant(a.getDate(), ZoneId.systemDefault())).distinct().collect(Collectors.toList());
    }
}
