package com.springprojects.securedoc.utils;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;

import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.entity.CredentialEntity;
import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.entity.UserEntity;
import com.springprojects.securedoc.exception.ApiException;

import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;

//import dev.samstevens.totp.code.HashingAlgorithm;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.springprojects.securedoc.constant.Constants.*;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class UserUtils {
	public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
		 return UserEntity.builder()
				 .userId(UUID.randomUUID().toString())
				 .firstName(firstName)
				 .lastName(lastName)
				 .email(email)
				 .lastLogin(now())
				 .accountNonExpired(true)
				 .accountNonLocked(true)
				 .mfa(false)
				 .enabled(false)
				 .loginAttempts(0)
				 .qrCodeSecret(EMPTY)
				 .phone(EMPTY)
				 .bio(EMPTY)
				 .imageUrl("https://cdn-icons-png.flaticon.com/128/2911/2911833.png")
				 .role(role)
				 .build();
	}
	
	public static User fromUserEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
			User user = new User();
			BeanUtils.copyProperties(userEntity, user);
			user.setLastLogin(userEntity.getLastLogin().toString());
			user.setCredentialsNonExpired(isCredentialNonExpired(credentialEntity));
			user.setCreatedAt(userEntity.getCreatedAt().toString());
			user.setUpdatedAt(userEntity.getUpdatedAt().toString());
			user.setRole(role.getName());
			user.setAuthorities(role.getAuthorities().getValue());
			return user;
	}

	public static boolean isCredentialNonExpired(CredentialEntity credentialEntity) {
		return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now());
	}
	
	public static BiFunction<String, String, QrData> qrDataFunction = (email, qrCodeSecret) -> 
	    new QrData.Builder()
	    .issuer(SPRING_SECURITY)
	    .label(email)
	    .secret(qrCodeSecret)
	    .algorithm(HashingAlgorithm.SHA1)
	    .digits(6)
	    .period(30)
	    .build();
	
	public static BiFunction<String, String, String> qrCodeImageUri = (email, qrCodeSecret) -> {
		var data = qrDataFunction.apply(email, qrCodeSecret);
		var generator = new ZxingPngQrGenerator();
		byte[] imageData;
		try {
			imageData = generator.generate(data);
		} catch(Exception exception) {
			throw new ApiException("Unable to create QR code URI");
		}
		return getDataUriForImage(imageData, generator.getImageMimeType());
	};
	
	public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate();
}