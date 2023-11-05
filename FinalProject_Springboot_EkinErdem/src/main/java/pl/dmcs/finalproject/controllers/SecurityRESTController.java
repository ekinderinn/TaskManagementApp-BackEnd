package pl.dmcs.finalproject.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/security")
public class SecurityRESTController {

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT') ")
    public String userAccess() {
        return ">>> Only students can access!";
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public String adminAccess() {
        return ">>> Only teachers can access!";
    }

}
