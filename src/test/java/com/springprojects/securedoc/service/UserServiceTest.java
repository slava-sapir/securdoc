package com.springprojects.securedoc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.springprojects.securedoc.entity.CredentialEntity;
import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.entity.UserEntity;
import com.springprojects.securedoc.enumeration.Authority;
import com.springprojects.securedoc.repository.CredentialRepository;
import com.springprojects.securedoc.repository.UserRepository;
import com.springprojects.securedoc.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	
  @Mock
  private UserRepository userRepository;
  @Mock
  private CredentialRepository credentialRepository;
  @InjectMocks
  private UserServiceImpl userServiceImpl;
  
  @Test
  @DisplayName("Test find user by ID")
  public void getUserByUserIdTest() {
	//	  Arrange -Given
	  var userEntity = new UserEntity();
	  userEntity.setFirstName("Slava");
	  userEntity.setId(1L);
	  userEntity.setCreatedAt(LocalDateTime.of(1990, 11, 1, 1, 1, 11));
	  userEntity.setUpdatedAt(LocalDateTime.of(1990, 11, 1, 1, 1, 11));
	  userEntity.setLastLogin(LocalDateTime.of(1990, 11, 1, 1, 1, 11));
	  
	  var roleEntity = new RoleEntity("USER", Authority.USER);
	  userEntity.setRole(roleEntity);
	  
	  var credentialEntity = new CredentialEntity();
	  credentialEntity.setUpdatedAt(LocalDateTime.of(1990, 11, 1, 1, 1, 11));
	  credentialEntity.setPassword("password");
	  credentialEntity.setUserEntity(userEntity);
	  
	  when(userRepository.findUserByUserId("1")).thenReturn(Optional.of(userEntity));
	  when(credentialRepository.getCredentialByUserEntityId(1L)).thenReturn(Optional.of(credentialEntity));
	//	  Act - When
	  var userByUserId = userServiceImpl.getUserByUserId("1");
	//	  Assert -Then
	  assertThat(userByUserId.getFirstName()).isEqualTo(userEntity.getFirstName());
  }
}
