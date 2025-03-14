package com.springprojects.securedoc.validation;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.springprojects.securedoc.entity.UserEntity;

public class UserValidation {
	
   public static void verifyAccountStatus(UserEntity user) {
	   if(!user.isEnabled()) { throw new DisabledException("Your account is currently disabled"); }
	   if(!user.isAccountNonExpired()) { throw new DisabledException("Your account has expired. Please contact administrator"); }
	   if(!user.isAccountNonLocked()) { throw new LockedException("Your account is currently locked"); }
       
   }
}
