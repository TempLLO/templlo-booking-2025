package com.templlo.service.review.event.external.producer;

import static com.templlo.service.review.event.external.topic.ProducerTopic.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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


	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Async
	@Override
	public void publishReviewCreated(ReviewCreatedEventDto eventDto) {

		try {
			CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(REVIEW_CREATED.toString(), eventDto);

			future.orTimeout(2000, TimeUnit.MILLISECONDS)
				.whenComplete((result, ex) -> {
					if (ex == null) {
						outBoxService.updateStatus(eventDto.reviewId(), true);
						log.info(String.format(EVENT_LOG, REVIEW_CREATED, eventDto));
					} else {
						log.error("kafka 메시지 전송 실패 응답 : {}", ex.getMessage());
						outBoxService.updateStatus(eventDto.reviewId(), false);
					}
				});

		} catch (Exception ex) {
			log.error("Failed to publish event because {}", ex.getMessage());
			outBoxService.updateStatus(eventDto.reviewId(), false);
		}

	}

	@Override
	public void publishReviewUpdated(ReviewUpdatedEventDto eventDto) {
		kafkaTemplate.send(REVIEW_UPDATED.toString(), eventDto);
		log.info(String.format(EVENT_LOG, REVIEW_UPDATED, eventDto));
	}
}
