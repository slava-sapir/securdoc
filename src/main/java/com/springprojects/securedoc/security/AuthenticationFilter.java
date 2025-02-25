package com.springprojects.securedoc.security;

import static org.springframework.http.HttpMethod.POST;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.springprojects.securedoc.domain.ApiAuthentication.unauthenticated;
import com.springprojects.securedoc.dtorequest.LoginRequest;
import com.springprojects.securedoc.enumeration.LoginType;
import com.springprojects.securedoc.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter{
	
	private final UserService userService;
	private final JwtService jwtService;
	
	
	protected AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
	    super(new AntPathRequestMatcher("/user/login", POST.name()), authenticationManager);
	    this.userService = userService;
	    this.jwtService = jwtService;
    }

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		try {
			var user = new ObjectMapper().configure(AUTO_CLOSE_JSON_CONTENT, true).readValue(request.getInputStream(), LoginRequest.class);
			userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_ATTEMPT);
			var authentication = unauthenticated(user.getEmail(), user.getPassword());
			return getAuthenticationManager().authenticate(authentication);
		} catch(Exception exception) {
			log.error(exception.getMessage());
			return null;
			}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
	}

}
