package com.v1.auth.repository;

import com.v1.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByAzureId(String azureId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
