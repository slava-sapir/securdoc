package com.springprojects.securedoc.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import com.springprojects.securedoc.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import static com.springprojects.securedoc.enumeration.TokenType.REFRESH;
import static com.springprojects.securedoc.enumeration.TokenType.ACCESS;

@Service
@RequiredArgsConstructor
public class ApiLogoutHandler implements LogoutHandler {
	private final JwtService jwtService;
	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		var logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(request, response, authentication);
		jwtService.removeCookie(request, response, ACCESS.getValue());
		jwtService.removeCookie(request, response, REFRESH.getValue());
	}
}
