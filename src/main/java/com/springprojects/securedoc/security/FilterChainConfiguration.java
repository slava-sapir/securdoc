package com.springprojects.securedoc.security;

import static com.google.common.net.HttpHeaders.X_REQUESTED_WITH;
import static com.springprojects.securedoc.constant.Constants.BASE_PATH;
import static com.springprojects.securedoc.constant.Constants.FILE_NAME;
import static com.springprojects.securedoc.constant.Constants.PUBLIC_URLS;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.springprojects.securedoc.handler.ApiAccessDeniedHandler;
import com.springprojects.securedoc.handler.ApiAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class FilterChainConfiguration {
	
	private final ApiAccessDeniedHandler apiAccessDeniedHandler;
	private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
	private final ApiHttpConfigurer apiHttpConfigurer;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(corsConfigurer -> corsConfigurer.configurationSource(null))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception.accessDeniedHandler(apiAccessDeniedHandler)
					.authenticationEntryPoint(apiAuthenticationEntryPoint))
			.authorizeHttpRequests( request -> 
		    request.requestMatchers(PUBLIC_URLS).permitAll()
		    .requestMatchers(OPTIONS).permitAll()
		    .requestMatchers(DELETE, "/user/delete/**")
		    .hasAnyAuthority("user:delete")
		    .requestMatchers(DELETE, "/document/delete/**")
		    .hasAnyAuthority("document:delete")
		    .anyRequest().authenticated())
			.with(apiHttpConfigurer, Customizer.withDefaults());
		    return http.build();
	}
	
	 @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
	        var corsConfiguration = new CorsConfiguration();
	        corsConfiguration.setAllowCredentials(true);
	        corsConfiguration.setAllowedOrigins(List.of("http://securedoc.com", "http://localhost:4200", "http://localhost:3000"));
	        corsConfiguration.setAllowedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, X_REQUESTED_WITH, ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS, FILE_NAME));
	        corsConfiguration.setExposedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, X_REQUESTED_WITH, ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS, FILE_NAME));
	        corsConfiguration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), PATCH.name(), DELETE.name(), OPTIONS.name()));
	        corsConfiguration.setMaxAge(3600L);
	        var source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration(BASE_PATH, corsConfiguration);
	        return source;
	    }
}
