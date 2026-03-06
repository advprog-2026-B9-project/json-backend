package com.b9.json.jsonplatform.auth.infrastructure.repository;

import com.b9.json.jsonplatform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}