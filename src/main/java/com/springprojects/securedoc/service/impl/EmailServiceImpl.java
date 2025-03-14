package com.springprojects.securedoc.service.impl;

import static com.springprojects.securedoc.utils.EmailUtils.getEmailMessage;
import static com.springprojects.securedoc.utils.EmailUtils.getResetPasswordMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.springprojects.securedoc.exception.ApiException;
import com.springprojects.securedoc.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
	private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
	private static final String PASSWORD_RESET_REQUEST = "Reset password message";
	private final JavaMailSender sender;
	@Value("${spring.mail.verify.host}")
	private String host;
	@Value("${spring.mail.username}")
	private String fromEmail;
	
	@Override
	@Async
	public void sendNewAccountEmail(String name, String email, String token) {
		try {
			var message = new SimpleMailMessage();
			message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
			message.setFrom(fromEmail);
			message.setTo(email);
			message.setText(getEmailMessage(name, host, token));
			sender.send(message);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			throw new ApiException("Unable to send email");
		}
	}

	@Override
	@Async
	public void sendPasswordResetEmail(String name, String email, String key) {
		try {
			var message = new SimpleMailMessage();
			message.setSubject(PASSWORD_RESET_REQUEST);
			message.setFrom(fromEmail);
			message.setTo(email);
			message.setText(getResetPasswordMessage(name, host, key));
			sender.send(message);
		} catch (Exception exception) {
			log.error(exception.getMessage());
			throw new ApiException("Unable to send email");
		}
	}
	
}
