package org.example.ecommerc_shop.repository;

import lombok.RequiredArgsConstructor;
import org.example.ecommerc_shop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsernameAndDeletedFalse(String username);
    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByPhoneNumberAndDeletedFalse(String phoneNumber);
    Page<User> findAllByDeletedFalse(Pageable pageable);
    Optional<User> findByIdAndDeletedFalse(String id);

    boolean existsByUsernameAndDeletedFalseAndIdNot(String username, String id);
    boolean existsByEmailAndDeletedFalseAndIdNot(String email, String id);

    boolean existsByPhoneNumberAndDeletedFalseAndIdNot(String phoneNumber, String id);
}
