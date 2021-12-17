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
public class ModflowDataControllerTest extends ModflowDataController implements DataController<PolygonDataOld> {

    private final ModflowDataConverter converter = new ModflowDataConverter();

    @CrossOrigin
    @GetMapping("/info")
    public ResponseEntity<Info> getInfo(HttpServletRequest request) {
        Info info = converter.getInfo();
        info.setId("modflowTest");
        info.setName("modflowTest");
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
