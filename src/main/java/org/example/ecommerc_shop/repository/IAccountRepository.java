package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IAccountRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
