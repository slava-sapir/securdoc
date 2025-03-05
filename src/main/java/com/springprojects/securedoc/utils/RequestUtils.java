package com.springprojects.securedoc.utils;

import static java.time.LocalDateTime.now;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springprojects.securedoc.domain.Response;
import com.springprojects.securedoc.exception.ApiException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestUtils {
	
	private static final BiConsumer<HttpServletResponse, Response> writeResponse = (httpServletResponse, response) -> {
		try {
			 var outputStream = httpServletResponse.getOutputStream();
			 new ObjectMapper().writeValue(outputStream, response);
			 outputStream.flush();
		} catch(Exception exception) {
		    throw new ApiException(exception.getMessage());	
		}
	};
	
	private static final BiFunction<Exception, HttpStatus, String> errorReason = (exception, httpStatus) -> {
		if(httpStatus.isSameCodeAs(FORBIDDEN)) { return "You do not have permission"; }
		if(httpStatus.isSameCodeAs(UNAUTHORIZED)) { return "You are not logged in"; }
		if(exception instanceof DisabledException || 
		   exception instanceof LockedException || 
		   exception instanceof BadCredentialsException ||
		   exception instanceof CredentialsExpiredException ||
		   exception instanceof ApiException ) {
			return exception.getMessage();
		}
		if(httpStatus.is5xxServerError()) { 
			return "An internal server error occured"; 
		}
		else { return "An error ocurred. Please ty again."; }
	};
   
	public static Response getResponse(HttpServletRequest request, Map<?,?> data, String message, HttpStatus status) {
		return new Response(
	    LocalDateTime.now().toString(), status.value(), 
	    request.getRequestURI(), HttpStatus.valueOf(status.value()), message, EMPTY, data); 
	}
	
	public static void handleErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception) {
		if(exception instanceof AccessDeniedException) {
			var apiResponse = getErrorResponse(request, response, exception, FORBIDDEN);
			writeResponse.accept(response, apiResponse);
		}
	}
	
	private static Response getErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception exception, HttpStatus status) {
		response.setContentType(APPLICATION_JSON_VALUE);
		response.setStatus(status.value());
		return new Response(now().toString(), status.value(), request.getRequestURI(), HttpStatus.valueOf(status.value()), errorReason.apply(exception, status), getRootCauseMessage(exception), emptyMap() );
	}
}
