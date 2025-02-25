package com.springprojects.securedoc.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MyOwnAuthenticationProvider implements  AuthenticationProvider {
	private final UserDetailsService userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		var user = (UsernamePasswordAuthenticationToken) authentication;
		var userFromBD = userDetailsService.loadUserByUsername((String)user.getPrincipal());
		var password = (String)user.getCredentials();
		if( password.equals(userFromBD.getPassword())) {
			return UsernamePasswordAuthenticationToken.authenticated(
					userFromBD, "[PASSWORD PROTECTED]", userFromBD.getAuthorities() );
		}
		throw new BadCredentialsException("Unable to login");
	}

	@Override
	public boolean supports(Class<?> authentication) {
	   return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
