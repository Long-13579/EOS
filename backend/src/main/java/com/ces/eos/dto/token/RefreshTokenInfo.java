package com.ces.eos.dto.token;

import com.ces.eos.entity.RefreshToken;
import com.ces.eos.entity.User;
import java.time.Instant;

public record RefreshTokenInfo(String token, String jti, Instant expiresAt) {
  public RefreshToken toEntity(User user) {
    return RefreshToken.builder().user(user).jti(jti).expiresAt(expiresAt).build();
  }
}
