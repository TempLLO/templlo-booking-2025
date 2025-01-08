package com.templlo.service.review.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.templlo.service.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
	Optional<Review> findByUserIdAndProgramId(UUID userId, UUID programId);

	Page<Review> findByUserId(UUID userId, Pageable pageable);

	Page<Review> findByProgramId(UUID programId, Pageable pageable);
}
