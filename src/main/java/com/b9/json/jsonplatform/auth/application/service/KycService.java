package com.b9.json.jsonplatform.auth.application.service;

import com.b9.json.jsonplatform.auth.domain.User;
import java.util.List;

public interface KycService {
    User submitKyc(String email, String fullName, String nikKtp, String ktpImageUrl);
    List<User> findPendingKyc();
    User reviewKyc(String email, boolean approved);
}