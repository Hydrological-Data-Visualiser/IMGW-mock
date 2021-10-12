package pl.edu.agh.imgwmock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class Controller {

    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity<String> allClasses(HttpServletRequest request) {
        return new ResponseEntity<>("asd", HttpStatus.OK);
    }
}
