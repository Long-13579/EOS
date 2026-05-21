package com.ces.eos.service;

import com.ces.eos.dto.google.GoogleUserInfo;

public interface GoogleOAuthService {
  GoogleUserInfo exchangeCodeForUserInfo(String code, String redirectUri);
}
