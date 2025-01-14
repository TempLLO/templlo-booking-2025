package com.templlo.service.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.templlo.service.user.common.exception.BaseException;
import com.templlo.service.user.common.jwt.util.TokenRedisUtil;
import com.templlo.service.user.common.response.BasicStatusCode;
import com.templlo.service.user.dto.UpdateUserRequestDto;
import com.templlo.service.user.entity.User;
import com.templlo.service.user.external.kafka.consumer.dto.ReviewCreatedEventDto;
import com.templlo.service.user.external.kafka.producer.UserEventProducer;
import com.templlo.service.user.external.kafka.producer.dto.ReviewRewardEventDto;
import com.templlo.service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModifyUserService {

	private final UserRepository userRepository;
	private final UserEventProducer userEventProducer;
	private final PasswordEncoder passwordEncoder;
	private final TokenRedisUtil tokenRedisUtil;

	@Transactional
	public void updateReviewCount(ReviewCreatedEventDto eventDto) {
		User targetUser = findUserByLoginId(eventDto.loginId());
		boolean needsCoupon = targetUser.increaseAndCheckReviewCount();

		if(needsCoupon) {
			ReviewRewardEventDto publishEventDto = new ReviewRewardEventDto(targetUser.getId());
			userEventProducer.publishReviewRewardCoupon(publishEventDto);
		}
	}

	@Transactional
	public void updateUser(UpdateUserRequestDto request, String accessToken, String loginId) {
		User targerUser = findUserByLoginId(loginId);
		String encodedPwd = passwordEncoder.encode(request.password());
		targerUser.updateUser(encodedPwd,  request.email(), request.userName(), request.nickName(),
			request.gender(), request.birth(), request.phone());

		tokenRedisUtil.addToBlacklist(accessToken, loginId, "updateUserInfo");
	}

	@Transactional
	public void deleteUser(String loginId, String accessToken) {
		User targetUser = findUserByLoginId(loginId);
		targetUser.deleteUser(loginId);
		tokenRedisUtil.addToBlacklist(accessToken, loginId, "deleteUser");
	}

	private User findUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BaseException(BasicStatusCode.USER_NOT_FOUND));
	}
}
