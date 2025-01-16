package com.templlo.service.review.event.dto;

import java.util.UUID;

public interface OutboxEvent {

	String getTopic();
	UUID getReviewId();
}
