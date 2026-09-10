package org.example.ecommerc_shop.repository;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
