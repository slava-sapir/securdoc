package com.springprojects.securedoc.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests( request -> 
		    request.requestMatchers("/user/login").permitAll().anyRequest().authenticated())
			.build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
		var myOwnAuthenticationProvider = new MyOwnAuthenticationProvider(userDetailsService);
		return new ProviderManager(myOwnAuthenticationProvider);
	}
	
//	@Bean
//	public UserDetailsService userDetailsService() {
//		
//		var slava = User.withDefaultPasswordEncoder()
//				.username("slava")
//				.password("{noop}letmein")
//				.roles("USER")
//				.build();
//		
//		var hanna = User.withDefaultPasswordEncoder()
//				.username("hanna")
//				.password("{noop}letmein")
//				.roles("USER")
//				.build();
//		
//		return new InMemoryUserDetailsManager(List.of(slava, hanna));
//	}
	
	@Bean
	public UserDetailsService inMemoryUserDetailsManager() {
		return new InMemoryUserDetailsManager(
			User.withUsername("slava").password("letmein").roles("USER").build(),
			User.withUsername("hanna").password("letmein").roles("USER").build()
		);
	}

}
