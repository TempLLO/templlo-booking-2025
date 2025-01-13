package com.templlo.service.promotion;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.templlo.service.common.client.UserFeignClient;
import com.templlo.service.common.dto.UserResponse;
import com.templlo.service.common.response.ApiResponse;
import com.templlo.service.coupon.controller.CouponController;
import com.templlo.service.coupon.dto.CouponIssueResponseDto;
import com.templlo.service.coupon.service.CouponService;

class CouponControllerTest {

	@InjectMocks
	private CouponController couponController;

	@Mock
	private CouponService couponService;

	@Mock
	private UserFeignClient userFeignClient;

	private MockMvc mockMvc;

	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(couponController).build();
		objectMapper = new ObjectMapper();
	}

	@Test
	void issueCoupon_Success() throws Exception {
		// Given
		String promotionId = "123e4567-e89b-12d3-a456-426614174000";
		String loginId = "test-login-id";
		String role = "MEMBER";
		UUID userId = UUID.randomUUID();

		// 수정된 CouponIssueResponseDto 생성
		CouponIssueResponseDto responseDto = new CouponIssueResponseDto(
			"SUCCESS",
			UUID.randomUUID(),
			"Coupon issued successfully"
		);

		// 수정된 UserResponse 생성
		UserResponse userResponse = new UserResponse(
			userId,
			loginId,
			"test-email",
			"test-user",
			"test-nickname",
			"MALE",
			"1990-01-01",
			role,
			"010-1234-5678",
			5
		);

		ApiResponse<UserResponse> userApiResponse = ApiResponse.ofSuccess(userResponse);

		when(userFeignClient.getUserByLoginId(loginId)).thenReturn(userApiResponse);
		when(couponService.issueCoupon(UUID.fromString(promotionId), null, userId, loginId)).thenReturn(responseDto);

		// When & Then
		mockMvc.perform(post("/api/coupons/issue")
				.param("promotionId", promotionId)
				.header("X-Login-Id", loginId)
				.header("X-User-Role", role)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.couponId").isNotEmpty())
			.andExpect(jsonPath("$.data.status").value("SUCCESS"));

		verify(userFeignClient, times(1)).getUserByLoginId(loginId);
		verify(couponService, times(1)).issueCoupon(UUID.fromString(promotionId), null, userId, loginId);
	}

	@Test
	void issueCoupon_MissingPromotionId() throws Exception {
		// Given
		String loginId = "test-login-id";
		String role = "MEMBER";

		// When & Then
		mockMvc.perform(post("/api/coupons/issue")
				.header("X-Login-Id", loginId)
				.header("X-User-Role", role)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value(
				"Required request parameter 'promotionId' for method parameter type String is not present"));
	}

	@Test
	void issueCoupon_UserNotFound() throws Exception {
		// Given
		String promotionId = "123e4567-e89b-12d3-a456-426614174000";
		String loginId = "test-login-id";
		String role = "MEMBER";

		when(userFeignClient.getUserByLoginId(loginId)).thenReturn(null);

		// When & Then
		mockMvc.perform(post("/api/coupons/issue")
				.param("promotionId", promotionId)
				.header("X-Login-Id", loginId)
				.header("X-User-Role", role)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.message").value("사용자 정보를 가져올 수 없습니다. loginId: " + loginId));

		verify(userFeignClient, times(1)).getUserByLoginId(loginId);
		verifyNoInteractions(couponService);
	}
}
