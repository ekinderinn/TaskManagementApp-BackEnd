package pl.dmcs.finalproject.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pl.dmcs.finalproject.message.request.LogInForm;
import pl.dmcs.finalproject.message.request.SignUpForm;
import pl.dmcs.finalproject.message.response.JwtResponse;
import pl.dmcs.finalproject.message.response.ResponseMessage;
import pl.dmcs.finalproject.model.Role;
import pl.dmcs.finalproject.model.RoleName;
import pl.dmcs.finalproject.model.User;
import pl.dmcs.finalproject.repository.RoleRepository;
import pl.dmcs.finalproject.repository.UserRepository;
import pl.dmcs.finalproject.security.jwt.JwtProvider;
import java.util.HashSet;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RequestMapping("/auth")
public class AuthRESTController {

    private DaoAuthenticationProvider daoAuthenticationProvider;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;

    @Autowired
    public AuthRESTController(DaoAuthenticationProvider daoAuthenticationProvider, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LogInForm loginRequest) {
        Authentication authentication = daoAuthenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt,userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpForm signUpRequest) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(new ResponseMessage("Fail -> Email is belong to someone else."), HttpStatus.BAD_REQUEST);
        }

        // Create user account
        User user = new User(signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            switch (role) {
                case "teacher":
                    Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Teacher not found."));
                    roles.add(teacherRole);
                    break;
                default:
                    Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                            .orElseThrow(() -> new RuntimeException("Fail -> Cause: Student not found."));
                    roles.add(studentRole);
            }
        });

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>(new ResponseMessage("User registered successfully."), HttpStatus.OK);

    }

}

