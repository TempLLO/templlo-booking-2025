package com.templlo.service.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.common.exception.BaseException;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.entity.User;
import com.templlo.service.user.external.kafka.consumer.dto.ReviewCreatedEventDto;
import com.templlo.service.user.external.kafka.producer.UserEventProducer;
import com.templlo.service.user.external.kafka.producer.dto.ReviewRewardEventDto;
import com.templlo.service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UpdateUserService {

	private final UserRepository userRepository;
	private final UserEventProducer userEventProducer;

	@Transactional
	public void updateReviewCount(ReviewCreatedEventDto eventDto) {
		User findUser = findUserByLoginId(eventDto.loginId());
		boolean needsCoupon = findUser.increaseAndCheckReviewCount();

		if(needsCoupon) {
			ReviewRewardEventDto publishEventDto = new ReviewRewardEventDto(findUser.getId());
			userEventProducer.publishReviewRewardCoupon(publishEventDto);
		}
	}

	private User findUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BaseException(BasicStatusCode.USER_NOT_FOUND));
	}

}
