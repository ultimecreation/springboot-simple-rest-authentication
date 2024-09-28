package com.authapi.demo.controller;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authapi.demo.dto.LoginDto;
import com.authapi.demo.dto.RegisterDto;
import com.authapi.demo.models.AppUser;
import com.authapi.demo.repository.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
public class AccountController {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication authentication) {
        var response = new HashMap<String, Object>();
        response.put("Username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());

        var appUser = appUserRepository.findByUsername(authentication.getName());
        response.put("user", appUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterDto registerDto, BindingResult result) {
        if (result.hasErrors()) {
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();
            for (int i = 0; i < errorsList.size(); i++) {
                var error = (FieldError) errorsList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorsMap);
        }

        var bcryptEncoder = new BCryptPasswordEncoder();

        AppUser appUser = new AppUser();
        appUser.setUsername(registerDto.getUsername());
        appUser.setEmail(registerDto.getEmail());
        appUser.setRole("Client");
        appUser.setPassword(bcryptEncoder.encode(registerDto.getPassword()));
        appUser.setCreatedAt(new Date());

        try {
            var usernameExists = appUserRepository.findByUsername(registerDto.getUsername());
            if (usernameExists != null) {
                return ResponseEntity.badRequest().body("Username already in use");
            }
            var emailExists = appUserRepository.findByEmail(registerDto.getEmail());
            if (emailExists != null) {
                return ResponseEntity.badRequest().body("Email already in use");
            }

            appUserRepository.save(appUser);
            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("user", appUser);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("'there was an error");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Error");
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDto loginDto, BindingResult result) {

        if (result.hasErrors()) {
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();
            for (int i = 0; i < errorsList.size(); i++) {
                var error = (FieldError) errorsList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorsMap);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            AppUser appUser = appUserRepository.findByUsername(loginDto.getUsername());
            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("user", appUser);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("'there was an error");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Bad Usernamle or password");
    }

    private String createJwtToken(AppUser appUser) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(24 * 3600))
                .subject(appUser.getUsername())
                .claim("role", appUser.getRole())
                .build();

        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey.getBytes()));
        var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return encoder.encode(params).getTokenValue();
    }
}
