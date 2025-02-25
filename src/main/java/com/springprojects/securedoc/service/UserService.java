package com.springprojects.securedoc.service;

import com.springprojects.securedoc.entity.RoleEntity;
import com.springprojects.securedoc.enumeration.LoginType;

public interface UserService {
   void createUser(String firstName, String lastName, String email, String password);
   RoleEntity getRoleName(String name);
   void verifyAccountKey(String key);
   void updateLoginAttempt(String email, LoginType loginType);
}
