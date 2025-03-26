package com.springprojects.securedoc.service;

import org.springframework.web.multipart.MultipartFile;

import com.springprojects.securedoc.dto.User;
import com.springprojects.securedoc.entity.CredentialEntity;
import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.enumeration.LoginType;
import java.util.List;

public interface UserService {
   void createUser(String firstName, String lastName, String email, String password);
   RoleEntity getRoleName(String name);
   void verifyAccountKey(String key);
   void updateLoginAttempt(String email, LoginType loginType);
   User getUserByEmail(String email);
   User getUserByUserId(String userId);
   CredentialEntity getUserCredentialById(Long userId);
   User setUpMfa(Long id);
   User cancelMfa(Long id);
   User verifyQrCode(String userId, String qrCode);
   void resetPassword(String email);
   User verifyPasswordKey(String key);
   void updatePassword(String userId, String newPassword, String confirmNewPassword);
   void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword);
   User updateUser(String userId, String firstName, String lastName, String email, String phone, String bio);
   void updateRole(String userId, String role);
   void toggleAccountExpired(String userId);
   void toggleAccountLocked(String userId);
   void toggleAccountEnabled(String userId);
   void toggleCredentialsExpired(String userId);
   String updatePhoto(String userId, MultipartFile file);
   List<User> getUsers();
   User getUserById(Long id);
}
