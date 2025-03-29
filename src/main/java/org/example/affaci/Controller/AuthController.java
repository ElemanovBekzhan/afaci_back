package org.example.affaci.Controller;


import org.example.affaci.Models.DTO.UserRegisterDTO;
import org.example.affaci.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> user_register(@RequestBody UserRegisterDTO userDTO) {
        if(userService.findByUsername(userDTO.getUsername())){
            return ResponseEntity.ok().body(HttpStatus.BAD_REQUEST);
        }
        userService.save(userDTO);
        return ResponseEntity.ok().body("User registered successfully");
    }

/*    @GetMapping("/login")
    public ResponseEntity<?> user_login(@RequestParam("username") String username, @RequestParam("password") String password) {

    }*/


}