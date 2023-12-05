package devwooki.study.querydsl.controller;

import jdk.jfr.Registered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class testController {
    @GetMapping
    public ResponseEntity<?> test(){
        return ResponseEntity.ok().body("test");
    }
}
