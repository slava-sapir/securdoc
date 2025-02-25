package com.springprojects.securedoc.resource;

import static com.springprojects.securedoc.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springprojects.securedoc.domain.Response;
import com.springprojects.securedoc.dtorequest.UserRequest;
import com.springprojects.securedoc.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserResource {
   
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {
    	userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
    	return ResponseEntity.created(getUri()).body(
    	getResponse(request, emptyMap(), "Account created.Check your email to enable your account", CREATED));
    }
    
    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request) {
    	userService.verifyAccountKey(key);
    	return ResponseEntity.ok().body(
    	getResponse(request, emptyMap(), "Account verified", OK));
    }
    
    private URI getUri() {
    	return URI.create("");
    }
}
