package com.springprojects.securedoc.security;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static com.springprojects.securedoc.constant.Constants.*;
import static com.springprojects.securedoc.domain.ApiAuthentication.unauthenticated;
import static com.springprojects.securedoc.utils.RequestUtils.handleErrorResponse;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static com.springprojects.securedoc.utils.RequestUtils.getResponse;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springprojects.securedoc.domain.Response;
import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.dtorequest.LoginRequest;
import com.springprojects.securedoc.enumeration.LoginType;
import com.springprojects.securedoc.enumeration.TokenType;
import com.springprojects.securedoc.service.JwtService;
import com.springprojects.securedoc.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiAuthenticationFilter extends AbstractAuthenticationProcessingFilter{

	private final UserService userService;
	private final JwtService jwtService;
	
	
	protected ApiAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
	    super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()), authenticationManager);
	    this.userService = userService;
	    this.jwtService = jwtService;
    }

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		try {
			var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
			userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_ATTEMPT);
			var authentication = unauthenticated(user.getEmail(), user.getPassword());
			return getAuthenticationManager().authenticate(authentication);
		} catch(Exception exception) {
			log.error(exception.getMessage());
			handleErrorResponse(request, response, exception);
			return null;
			}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {
		var user = (User) authentication.getPrincipal();
		userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_SUCCESS);
		var httpResponse = user.isMfa() ? sendQrCode(request, user) : sendResponse(request, response, user);
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(OK.value());
		var out = response.getOutputStream();
		var mapper = new ObjectMapper();
		mapper.writeValue(out, httpResponse);
		out.flush();
	}
	
	private Response sendResponse(HttpServletRequest request, HttpServletResponse response, User user) {
		jwtService.addCookie(response, user, TokenType.ACCESS);
		jwtService.addCookie(response, user, TokenType.REFRESH);
		return getResponse(request, Map.of("user", user), "Login Success", OK);
	}

	private Response sendQrCode(HttpServletRequest request, User user) {
		return getResponse(request, Map.of("user", user), "Please enter QR code", OK);
	}
}
