package com.springprojects.securedoc.security;

import static com.springprojects.securedoc.constant.Constants.PUBLIC_ROUTES;
import static com.springprojects.securedoc.enumeration.TokenType.ACCESS;
import static com.springprojects.securedoc.enumeration.TokenType.REFRESH;
import static com.springprojects.securedoc.utils.RequestUtils.handleErrorResponse;
import static io.jsonwebtoken.lang.Arrays.asList;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springprojects.securedoc.domain.ApiAuthentication;
import com.springprojects.securedoc.domain.RequestContext;
import com.springprojects.securedoc.domain.Token;
import com.springprojects.securedoc.domain.TokenData;
import com.springprojects.securedoc.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			var accessToken = jwtService.extractToken(request, ACCESS.getValue());
			if(accessToken.isPresent() && jwtService.getTokenData(accessToken.get(), TokenData::isValid)) {
				SecurityContextHolder.getContext().setAuthentication(getAuthentication(accessToken.get(), request));
			    RequestContext.setUserId(jwtService.getTokenData(accessToken.get(), TokenData::getUser).getId());
			} else {
				var refreshToken = jwtService.extractToken(request, REFRESH.getValue());
				if(refreshToken.isPresent() && jwtService.getTokenData(refreshToken.get(), TokenData::isValid)) {
				   var user = jwtService.getTokenData(refreshToken.get(), TokenData::getUser);
				   SecurityContextHolder.getContext().setAuthentication(getAuthentication(jwtService.createToken(user, Token::getAccess), request));
				   jwtService.addCookie(response, user, ACCESS);
				   RequestContext.setUserId(user.getId());
				} else {
					SecurityContextHolder.clearContext();
				}
			}
			filterChain.doFilter(request, response);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			handleErrorResponse(request, response, exception);
		}
		
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
			var shouldNotFilter = request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name()) || 
			asList(PUBLIC_ROUTES).contains(request.getRequestURI());
			if(shouldNotFilter) { RequestContext.setUserId(0L); }
			return shouldNotFilter;
	}

	private Authentication getAuthentication(String token, HttpServletRequest request) {
	 var authentication = ApiAuthentication.authenticated(jwtService.getTokenData(token, TokenData::getUser),
			 jwtService.getTokenData(token, TokenData::getAuthorities));
	     authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	     return authentication;
	}
}
