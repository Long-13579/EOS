package com.ces.eos.security;

import com.ces.eos.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final UUID id;
  private final String email;
  private final String firstName;
  private final String lastName;
  private final String role;
  private final Collection<? extends GrantedAuthority> authorities;

  private CustomUserDetails(UUID id, String email, String firstName, String lastName, String role) {
    this.id = id;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.role = role;
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  public static CustomUserDetails fromUser(User user) {
    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getRole().getName().name());
  }

  @Override
  public String getPassword() {
    return null; // OAuth2 authentication - no password stored
  }

  @Override
  public String getUsername() {
    return email;
  }
}
