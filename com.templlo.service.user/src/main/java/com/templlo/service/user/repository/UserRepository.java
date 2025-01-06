package com.templlo.service.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.templlo.service.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	boolean existsByLoginId(String loginId);

	Optional<User> findByLoginId(String loginId);
}
