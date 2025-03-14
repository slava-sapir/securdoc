package com.springprojects.securedoc.service.impl;

import static com.springprojects.securedoc.utils.UserUtils.createUserEntity;
import static com.springprojects.securedoc.utils.UserUtils.*;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.springprojects.securedoc.validation.UserValidation.verifyAccountStatus;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.springprojects.securedoc.cashe.CacheStore;
import com.springprojects.securedoc.domain.RequestContext;
import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.entity.ConfirmationEntity;
import com.springprojects.securedoc.entity.CredentialEntity;
import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.entity.UserEntity;
import com.springprojects.securedoc.enumeration.Authority;
import com.springprojects.securedoc.enumeration.EventType;
import com.springprojects.securedoc.enumeration.LoginType;
import com.springprojects.securedoc.event.UserEvent;
import com.springprojects.securedoc.exception.ApiException;
import com.springprojects.securedoc.repository.ConfirmationRepository;
import com.springprojects.securedoc.repository.CredentialRepository;
import com.springprojects.securedoc.repository.RoleRepository;
import com.springprojects.securedoc.repository.UserRepository;
import com.springprojects.securedoc.service.UserService;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final BCryptPasswordEncoder encoder;
    private final CacheStore<String, Integer> userCache;
    private final ApplicationEventPublisher publisher;
    
	@Override
	public void createUser(String firstName, String lastName, String email, String password) {
	     var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
	     var credentialEntity = new CredentialEntity(userEntity, encoder.encode(password));
	     credentialRepository.save(credentialEntity);
	     var confirmationEntity = new ConfirmationEntity(userEntity);
	     confirmationRepository.save(confirmationEntity);
	     publisher.publishEvent(new UserEvent(userEntity, 
	    		 EventType.REGISTRATION, Map.of("key", confirmationEntity.getKey())));
	}
	
	@Override
	public RoleEntity getRoleName(String name) {
		var role = roleRepository.findByNameIgnoreCase(name);
		return role.orElseThrow( () -> new ApiException("Role not found"));
	}
	
	@Override
	public void verifyAccountKey(String key) {
		var confirmationEntity = getUserConfirmation(key);
		var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
		userEntity.setEnabled(true);
		userRepository.save(userEntity);
		confirmationRepository.delete(confirmationEntity);
	}
	
    @Override
	public void updateLoginAttempt(String email, LoginType loginType) {
	   	var userEntity = getUserEntityByEmail(email);
	   	RequestContext.setUserId(userEntity.getId());
	   	switch (loginType) {
		   	case LOGIN_ATTEMPT -> {
		   		if(userCache.get(userEntity.getEmail()) == null) {
		   			userEntity.setLoginAttempts(0);
		   			userEntity.setAccountNonLocked(true);
		   		}
		   		userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
		   		userCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());
		   		if(userCache.get(userEntity.getEmail()) > 5) {
		   			userEntity.setAccountNonLocked(false);
		   		}
		   	}
		   	case LOGIN_SUCCESS -> {
		   		userEntity.setAccountNonLocked(true);
		   		userEntity.setLoginAttempts(0);
		   		userEntity.setLastLogin(now());
		   		userCache.evict(userEntity.getEmail());
		   	}
	   	}
	   	userRepository.save(userEntity);
	}
    
    @Override
   	public User getUserByUserId(String userId) {
    	var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found"));
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
   	}
    
    @Override
	public User getUserByEmail(String email) {
		UserEntity userEntity = getUserEntityByEmail(email);
		return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
	}
    
	@Override
    public CredentialEntity getUserCredentialById(Long userId) {
        var credentialById = credentialRepository.getCredentialByUserEntityId(userId);
        return credentialById.orElseThrow(() -> new ApiException("Unable to find user credential"));
    }

    @Override
    public User setUpMfa(Long id) {
        var userEntity = getUserEntityById(id);
        var codeSecret = qrCodeSecret.get();
        userEntity.setQrCodeImageUri(qrCodeImageUri.apply(userEntity.getEmail(), codeSecret));
        userEntity.setQrCodeSecret(codeSecret);
        userEntity.setMfa(true);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

	@Override
	public User cancelMfa(Long id) {
		var userEntity = getUserEntityById(id);
		userEntity.setMfa(false);
		userEntity.setQrCodeSecret(EMPTY);
		userEntity.setQrCodeImageUri(EMPTY);
		userRepository.save(userEntity);
		return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
	}	
	
	@Override
	public User verifyQrCode(String userId, String qrCode) {
		var userEntity = getUserEntityByUserId(userId);
		verifyCode(qrCode, userEntity.getQrCodeSecret());
		return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
	}
	
	@Override
	public void resetPassword(String email) {
		var user = getUserEntityByEmail(email);	
		var confirmation = getUserConfirmation(user);
		if(confirmation != null) {
			 publisher.publishEvent(new UserEvent(user, EventType.RESETPASSWORD, Map.of("key", confirmation.getKey())));
		} else {
			var confirmationEntity = new ConfirmationEntity(user);
			confirmationRepository.save(confirmationEntity);
		    publisher.publishEvent(new UserEvent(user, EventType.RESETPASSWORD, Map.of("key", confirmationEntity.getKey())));	
		}
	}

	@Override
	public User verifyPasswordKey(String key) {
		var confirmationEntity = getUserConfirmation(key);
		if(confirmationEntity == null) { throw new ApiException("Unable to find token"); }
		var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
		if(userEntity == null) { throw new ApiException("Incorrect token"); }
		verifyAccountStatus(userEntity);
		confirmationRepository.delete(confirmationEntity);
		return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
	}
	
	@Override
	public void updatePassword(String userId, String newPassword, String confirmNewPassword) {
		if( !confirmNewPassword.equals(confirmNewPassword)) { throw new ApiException("Passwords don't match. Please try again"); }
		var user = getUserByUserId(userId);
		var credentials = getUserCredentialById(user.getId());
		credentials.setPassword(encoder.encode(newPassword));
		credentialRepository.save(credentials);
	}
	
	@Override
	public User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio) {
		var userEntity = getUserEntityByUserId(userId);
		userEntity.setFirstName(firstName);
		userEntity.setLastName(lastName);
		userEntity.setEmail(email);
		userEntity.setPhone(phone);
		userEntity.setBio(bio);
		userRepository.save(userEntity);
		return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
	}

	@Override
	public void updateRole(String userId, String role) {
		var userEntity = getUserEntityByUserId(userId);
		userEntity.setRole(getRoleName(role));
		userRepository.save(userEntity);
	}
	
	private UserEntity getUserEntityByEmail(String email) {
        var userByEmail = userRepository.findByEmailIgnoreCase(email);
        return userByEmail.orElseThrow(() -> new ApiException("User not found"));
	}

	private UserEntity getUserEntityById(Long id) {
	        var userById = userRepository.findById(id);
	        return userById.orElseThrow(() -> new ApiException("User not found"));
	}
	 
	private UserEntity createNewUser(String firstName, String lastName, String email) {
		var role = getRoleName(Authority.USER.name());
		return createUserEntity(firstName, lastName, email, role);
	}
	
	private ConfirmationEntity getUserConfirmation(String key) {
		return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation key not found"));
	}
	
	private ConfirmationEntity getUserConfirmation(UserEntity user) {
		return confirmationRepository.findByUserEntity(user).orElse(null);
	}
	
	private boolean verifyCode(String qrCode, String qrCodeSecret) {
		TimeProvider timeProvider = new SystemTimeProvider();
		CodeGenerator codeGenerator = new DefaultCodeGenerator();		
		CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
		if(codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
			return true;
		} else {
			throw new ApiException("Invalid QR code. Please try again");
		}
	}

	private UserEntity getUserEntityByUserId(String userId) {
		var userByUserId = userRepository.findUserByUserId(userId);
	    return userByUserId.orElseThrow(() -> new ApiException("User not found"));
	}
}
