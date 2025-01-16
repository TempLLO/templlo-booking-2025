package com.templlo.service.review.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.templlo.service.review.entity.ReviewOutbox;

@Repository
public interface ReviewOutboxRepository extends JpaRepository<ReviewOutbox, Long> {
	Optional<ReviewOutbox> findByReviewId(UUID reviewId);
}
