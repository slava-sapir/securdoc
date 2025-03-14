package com.springprojects.securedoc.dtorequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrCodeRequest {
	
  @NotEmpty(message = "User ID cannot be empty or null")
  private String userId;
  @NotEmpty(message = "QR Code cannot be empty or null")
  private String qrCode;
}
