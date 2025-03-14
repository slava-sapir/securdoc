package com.springprojects.securedoc.resource;

import static com.springprojects.securedoc.utils.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static com.springprojects.securedoc.enumeration.TokenType.REFRESH;
import static com.springprojects.securedoc.enumeration.TokenType.ACCESS;
import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springprojects.securedoc.domain.Response;
import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.dtorequest.EmailRequest;
import com.springprojects.securedoc.dtorequest.QrCodeRequest;
import com.springprojects.securedoc.dtorequest.ResetPasswordRequest;
import com.springprojects.securedoc.dtorequest.RoleRequest;
import com.springprojects.securedoc.dtorequest.UserRequest;
import com.springprojects.securedoc.enumeration.TokenType;
import com.springprojects.securedoc.service.JwtService;
import com.springprojects.securedoc.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserResource {
   
    private final UserService userService;
    private final JwtService jwtService;
    
    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request) {
    	userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
    	return ResponseEntity.created(getUri()).body(
    	getResponse(request, emptyMap(), "Account created.Check your email to enable your account", CREATED));
    }
    
    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, 
    		HttpServletRequest request) throws InterruptedException {
        userService.verifyAccountKey(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account verified.", OK));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal User userPrincipal, 
    		 HttpServletRequest request) {
        var user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "Profile retrieved", OK));
    }
    
    @PatchMapping("/update")
    public ResponseEntity<Response> update(@AuthenticationPrincipal User userPrincipal, 
    		@RequestBody UserRequest userRequest, HttpServletRequest request) {
        var user = userService.updateUser(userPrincipal.getUserId(), userRequest.getFirstName(), 
        	userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio() );
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "User profile updated successfully", OK));
    }
    
    @PatchMapping("/updaterole")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal User userPrincipal, 
    		@RequestBody RoleRequest roleRequest, HttpServletRequest request) {
           userService.updateRole(userPrincipal.getUserId(), roleRequest.getRole() );
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User Role updated successfully", OK));
    }
    
    
    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setUpMfa(@AuthenticationPrincipal User userPrincipal, 
    		HttpServletRequest request) {
        var user = userService.setUpMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA set up successfully", OK));
    }
    
    @PatchMapping("/mfa/cancel")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal User userPrincipal, 
    		HttpServletRequest request) {
    	var user = userService.cancelMfa(userPrincipal.getId());
    	return ResponseEntity.ok().body(
    	getResponse(request, of("user", user), "MFA cancel successfull", OK));
    }
    
    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrcode(@RequestBody QrCodeRequest qrCodeRequest, 
    		HttpServletRequest request, HttpServletResponse response) {
    	var user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());
    	jwtService.addCookie(response, user, ACCESS);
		jwtService.addCookie(response, user, REFRESH);
    	return ResponseEntity.ok().body(
    	getResponse(request, of("user", user), "QR Code verifyed", OK));
    }
    
    //START -Reset password when user is not logged in
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid EmailRequest emailRequest, 
    		HttpServletRequest request) {
    	userService.resetPassword(emailRequest.getEmail());
    	return ResponseEntity.ok().body(
    	getResponse(request, emptyMap(), "We sent you an email to reset your password", OK));
    }
    
    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key, 
    		HttpServletRequest request) {
    	var user = userService.verifyPasswordKey(key);
    	return ResponseEntity.ok().body(
    	getResponse(request, of("user", user), "Enter new password", OK));
    }
    
    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> doResetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
    	userService.updatePassword(resetPasswordRequest.getUserId(), 
    			resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
    	return ResponseEntity.ok().body(
    	getResponse(request, emptyMap(), "Password reset succesfully", OK));
    }//END -Reset password when user is not logged in
    
    private URI getUri() {
    	return URI.create("");
    }
}
