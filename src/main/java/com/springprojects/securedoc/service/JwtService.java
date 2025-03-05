package com.springprojects.securedoc.service;

import com.springprojects.securedoc.domain.Token;
import com.springprojects.securedoc.domain.TokenData;
import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.enumeration.TokenType;

//import com.springprojects.securedoc.enumeration.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {
   String createToken(User user, Function<Token, String> tokenFunction);
   Optional <String> extractToken(HttpServletRequest request, String tokenType);
   void addCookie(HttpServletResponse response, User user, TokenType type);
   <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);
   void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
}
