package com.springprojects.securedoc.service.impl;

import static com.springprojects.securedoc.constant.Constants.AUTHORITIES;
import static com.springprojects.securedoc.constant.Constants.AUTHORITY_DELIMITER;
import static com.springprojects.securedoc.constant.Constants.EMPTY_VALUE;
import static com.springprojects.securedoc.constant.Constants.ROLE;
import static com.springprojects.securedoc.constant.Constants.ROLE_PREFIX;
import static com.springprojects.securedoc.constant.Constants.SPRING_SECURITY;
import static com.springprojects.securedoc.enumeration.TokenType.ACCESS;
import static com.springprojects.securedoc.enumeration.TokenType.REFRESH;
import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static java.time.Instant.now;
import static java.util.Arrays.stream;
import static java.util.Date.from;
import static java.util.Optional.empty;
import static org.springframework.boot.web.server.Cookie.SameSite.NONE;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.springprojects.securedoc.domain.Token;
import com.springprojects.securedoc.domain.TokenData;
import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.enumeration.TokenType;
import com.springprojects.securedoc.function.TriConsumer;
import com.springprojects.securedoc.security.JwtConfiguration;
import com.springprojects.securedoc.service.JwtService;
import com.springprojects.securedoc.service.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl extends JwtConfiguration implements JwtService {
    
	private final UserService userService;
	
	private final Supplier<SecretKey> key = () -> 
	   Keys.hmacShaKeyFor(Decoders.BASE64.decode(getSecret()));
	
	private final Function<String, Claims> claimsFunction = token ->
	   Jwts.parser().verifyWith(key.get())
			   .build()
			   .parseSignedClaims(token)
			   .getPayload();
	
	private final Function<String, String>  subject = token -> getClaimsValue(token, Claims::getSubject);
	
	private final BiFunction<HttpServletRequest, String, Optional<String>> extractToken = (request, cookieName) -> 
	   Optional.of( stream(request.getCookies() == null ? 
			   new Cookie[]{ new Cookie(EMPTY_VALUE, EMPTY_VALUE) } : 
			   request
			   .getCookies())
			   .filter(cookie -> Objects.equals(cookieName, cookie.getName()))
			   .map(Cookie::getValue)
			   .findAny())
	           .orElse(empty());
	
	private final BiFunction<HttpServletRequest, String, Optional<Cookie>> extractCookie = (request, cookieName) ->
	   Optional.of( stream(request.getCookies() == null ? 
			   new Cookie[]{ new Cookie(EMPTY_VALUE, EMPTY_VALUE) } : 
			   request.getCookies())
			   .filter(cookie -> Objects.equals(cookieName, cookie.getName()))
			   .findAny()).orElse(empty());
	   
	private final Supplier<JwtBuilder> builder = () ->
	   Jwts.builder()
		       .header().add(Map.of(TYPE, JWT_TYPE))
		       .and()
		       .audience().add(SPRING_SECURITY)
		       .and()
		       .id(UUID.randomUUID().toString())
		       .issuedAt(from(now()))
		       .notBefore(new Date())
		       .signWith(key.get(), Jwts.SIG.HS512);
	   
	private final BiFunction<User, TokenType, String> buildToken = (user, type) ->
	    Objects.equals(type, ACCESS) ? 
	    	   builder
	    	   .get()
	    	   .subject(user.getUserId())
	    	   .claim(AUTHORITIES, user.getAuthorities())
	    	   .claim(ROLE, user.getRole())
	    	   .expiration(from(now().plusSeconds(getExpiration()))).compact() :
    		   builder
    		   .get()
	    	   .subject(user.getUserId())
	    	   .expiration(from(now().plusSeconds(getExpiration()))).compact();
	    
	private final TriConsumer<HttpServletResponse, User, TokenType> addCookie = (response, user, type) -> {
		switch (type) {
		case ACCESS -> {
			var accessToken = createToken(user, Token::getAccess);
			var cookie = new Cookie(type.getValue(), accessToken);
			cookie.setHttpOnly(true);
          //cookie.setSecure(true);
			cookie.setMaxAge(2*60);
			cookie.setPath("/");
			cookie.setAttribute("SameSite", NONE.name());
			response.addCookie(cookie);
		 }
		case REFRESH -> {
			var refreshToken = createToken(user, Token::getRefresh);
			var cookie = new Cookie(type.getValue(), refreshToken);
			cookie.setHttpOnly(true);
          //cookie.setSecure(true);
			cookie.setMaxAge(2*60*60);
			cookie.setPath("/");
			cookie.setAttribute("SameSite", NONE.name());
			response.addCookie(cookie);
		 }
		}
		
	};
	
	private <T> T getClaimsValue(String token, Function<Claims, T> claims) {
		return claimsFunction.andThen(claims).apply(token);
	}
	
	public Function<String, List<GrantedAuthority>> authorities = token ->
	           commaSeparatedStringToAuthorityList(new StringJoiner(AUTHORITY_DELIMITER)
	    	   .add(claimsFunction.apply(token).get(AUTHORITIES, String.class))
	           .add(ROLE_PREFIX + claimsFunction.apply(token).get(ROLE, String.class)).toString());
	           
	@Override
	public String createToken(User user, Function<Token, String> tokenFunction) {
		var token = Token.builder()
				.access(buildToken.apply(user, ACCESS))
				.refresh(buildToken.apply(user, REFRESH))
				.build();
		return tokenFunction.apply(token);
	}

	@Override
	public Optional<String> extractToken(HttpServletRequest request, String cookieName) {
		return extractToken.apply(request, cookieName);
	}

	@Override
	public void addCookie(HttpServletResponse response, User user, TokenType type) {
		addCookie.accept(response, user, type);
	}

	@Override
    public <T> T getTokenData(String token, Function<TokenData, T> tokenFunction) {
        return tokenFunction.apply(
            TokenData.builder()
                .valid(Objects.equals(userService.getUserByUserId(subject.apply(token)).getUserId(), claimsFunction.apply(token).getSubject()))
                .authorities(authorities.apply(token))
                .claims(claimsFunction.apply(token))
                .user(userService.getUserByUserId(subject.apply(token)))
                .build());
    }

	@Override
	public void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
	   var optionalCookie = extractCookie.apply(request, cookieName);
	   if(optionalCookie.isPresent()) {
		   var cookie = optionalCookie.get();
		   cookie.setMaxAge(0);
		   response.addCookie(cookie);
	   }
	}
}
