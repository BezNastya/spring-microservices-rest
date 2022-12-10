package com.example.usermodule.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.usermodule.AuthenticationRequestDto;
import com.example.usermodule.Role;
import com.example.usermodule.User;
import com.example.usermodule.repositories.RoleRepository;
import com.example.usermodule.security.jwt.JwtTokenProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.usermodule.repositories.UserRepository;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthenticationRestControllerV1 {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MeterRegistry meterRegistry;
    private final Timer authTimer;

    @Autowired
    public AuthenticationRestControllerV1(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                                          UserRepository userRepository, RoleRepository roleRepository, MeterRegistry meterRegistry) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.roleRepository=roleRepository;
        this.meterRegistry = meterRegistry;
        this.authTimer = this.meterRegistry.timer("auth.timer");
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationRequestDto requestDto) {
        try {
            return authTimer.recordCallable(() -> tryLoginUser(requestDto));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("Exception occurred during authentication:", e);
            throw new RuntimeException("Server error occured");
        }
    }
    @PostMapping("/register")
    public User register(@RequestBody AuthenticationRequestDto requestDto) {
            User user = new User();
            user.setLogin(requestDto.getLogin());
            user.setPassword(requestDto.getPassword());
            userRepository.save(user);
            return user;
    }
    @PostMapping("/registerAdmin")
    public User registerAdmin(@RequestBody AuthenticationRequestDto requestDto) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        User admin = new User();
        admin.setLogin(requestDto.getLogin());
        admin.setPassword(requestDto.getPassword());
        admin.setRoles(List.of(adminRole));
        userRepository.save(admin);
        return admin;
    }

    private ResponseEntity<Map<Object, Object>> tryLoginUser(AuthenticationRequestDto requestDto) {
        String login = requestDto.getLogin();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, requestDto.getPassword()));
        User user = userRepository.findUserByLogin(login);

        if (user == null) {
            throw new UsernameNotFoundException("User with login: " + login + " not found");
        }

        String token = jwtTokenProvider.createToken(login, user.getRoles());

        Map<Object, Object> response = new HashMap<>();
        response.put("login", login);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }
}