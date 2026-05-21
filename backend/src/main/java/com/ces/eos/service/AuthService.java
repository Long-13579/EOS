package com.ces.eos.service;

import com.ces.eos.dto.request.GoogleAuthRequest;
import com.ces.eos.dto.token.TokenPair;

public interface AuthService {
  TokenPair loginWithGoogle(GoogleAuthRequest request);

  TokenPair refreshToken(String refreshToken);

  void logout(String accessToken, String refreshToken);
}
