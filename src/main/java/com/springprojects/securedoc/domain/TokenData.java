package com.springprojects.securedoc.domain;

import java.util.List;

import com.springprojects.securedoc.dto.User;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Builder
@Getter
@Setter
public class TokenData {
  private User user;
  private Claims claims;
  private boolean valid;
  private List<GrantedAuthority> authorities;
}
