package com.templlo.service.review.event.external.producer;

import static com.templlo.service.review.event.topic.ProducerTopic.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.review.entity.ReviewOutbox;
import com.templlo.service.review.event.dto.ReviewCreatedEventDto;
import com.templlo.service.review.event.dto.ReviewUpdatedEventDto;
import com.templlo.service.review.service.ReviewOutboxService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j(topic = "Review External Event Producer ")
@RequiredArgsConstructor
public class ReviewExternalEventProducerImpl implements ReviewExternalEventProducer {

	private final String EVENT_LOG = " *** [Topic] %s, [Message] %s";
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final ReviewOutboxService outBoxService;
	private final ObjectMapper objectMapper;


	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	@Override
	public void publishReviewCreated(ReviewOutbox outbox) {

		try {
			ReviewCreatedEventDto payload = objectMapper.readValue(outbox.getPayload(), ReviewCreatedEventDto.class);

			kafkaTemplate.send(outbox.getEventType(), payload)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						outBoxService.updateStatus(outbox.getReviewId(), true);
						log.info(String.format(EVENT_LOG, REVIEW_CREATED, outbox.getPayload()));
					} else {
						log.error("Get error msg from Kafka : {}", ex.getMessage());
						outBoxService.updateStatus(outbox.getReviewId(), false);
					}
				});

		} catch (Exception ex) {
			log.error("Failed to publish event because {}", ex.getMessage());
			outBoxService.updateStatus(outbox.getReviewId(), false);
		}

	}

	@Override
	public void publishReviewUpdated(ReviewUpdatedEventDto eventDto) {
		kafkaTemplate.send(REVIEW_UPDATED.toString(), eventDto);
		log.info(String.format(EVENT_LOG, REVIEW_UPDATED, eventDto));
	}
}
