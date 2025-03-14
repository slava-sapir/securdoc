package com.springprojects.securedoc.domain;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_DEFAULT)
public record Response(
 String time, int code, String path, HttpStatus status, String message, 
 String exception, Map<?, ?> data) {}
