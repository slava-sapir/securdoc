package com.springprojects.securedoc.utils;

import java.util.UUID;

import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.entity.UserEntity;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;

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
}