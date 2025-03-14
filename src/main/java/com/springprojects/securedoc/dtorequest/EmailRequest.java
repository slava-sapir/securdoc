package com.springprojects.securedoc.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailRequest {
	
  @NotEmpty(message = "Email name cannot be empty or null")
  @Email(message = "Invalid email address")
  private String email;
}
