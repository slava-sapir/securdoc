package com.springprojects.securedoc.service.impl;

import static com.springprojects.securedoc.utils.UserUtils.createUserEntity;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.springprojects.securedoc.cashe.CacheStore;
import com.springprojects.securedoc.domain.RequestContext;
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
//    private final BCryptPasswordEncoder encoder;
    private final CacheStore<String, Integer> userCache;
    private final ApplicationEventPublisher publisher;
    
	@Override
	public void createUser(String firstName, String lastName, String email, String password) {
	     var userEntity = userRepository.save(createNewUser(firstName, lastName, email));
	     var credentialEntity = new CredentialEntity(userEntity, password);
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
	
	private UserEntity createNewUser(String firstName, String lastName, String email) {
		var role = getRoleName(Authority.USER.name());
		return createUserEntity(firstName, lastName, email, role);
	}

	@Override
	public void verifyAccountKey(String key) {
		var confirmationEntity = getUserConfirmation(key);
		var userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
		userEntity.setEnabled(true);
		userRepository.save(userEntity);
		confirmationRepository.delete(confirmationEntity);
		
	}

	private UserEntity getUserEntityByEmail(String email) {
		var userByEmail = userRepository.findByEmailIgnoreCase(email);
		return userByEmail.orElseThrow(() -> new ApiException("User not found"));
	}

	private ConfirmationEntity getUserConfirmation(String key) {
		return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Confirmation key not found"));
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
		   		userCache.evict(userEntity.getEmail());
		   	}
	   	}
	   	userRepository.save(userEntity);
	}
}
