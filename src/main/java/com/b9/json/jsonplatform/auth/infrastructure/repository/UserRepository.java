package com.b9.json.jsonplatform.auth.infrastructure.repository;

import com.b9.json.jsonplatform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    User findByUsername(String username);
}