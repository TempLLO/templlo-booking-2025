package com.templlo.service.review.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.templlo.service.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
	Optional<Review> findByUserIdAndProgramId(UUID userId, UUID programId);
}
