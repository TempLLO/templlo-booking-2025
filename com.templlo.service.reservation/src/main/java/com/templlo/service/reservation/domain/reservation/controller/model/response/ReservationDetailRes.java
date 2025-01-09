package com.templlo.service.reservation.domain.reservation.controller.model.response;

import com.templlo.service.reservation.domain.reservation.controller.model.request.CouponUsedType;
import com.templlo.service.reservation.domain.reservation.domain.*;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record ReservationDetailRes(
        UUID reservationId,
        UUID programId,
        String programDate,
        UUID userId,
        ReservationStatus status,
        String name,
        String phoneNumber,
        ReservationGenderType gender,
        CouponUsedType couponUsedType,
        UUID couponId,
        PaymentStatus paymentStatus,
        PaymentType paymentType,
        LocalDateTime createdAt,
        String createdBy
) {
    public static ReservationDetailRes from(Reservation reservation) {
        CouponUsedType couponUsedType = CouponUsedType.valueOfIsUsed(reservation.isCouponUsed());
        return ReservationDetailRes.builder()
                .reservationId(reservation.getReservationId())
                .programId(reservation.getProgramId())
                .programDate(reservation.getProgramDateFormatted())
                .userId(reservation.getUserId())
                .status(reservation.getStatus())
                .name(reservation.getName())
                .phoneNumber(reservation.getPhoneNumber())
                .gender(reservation.getGender())
                .couponUsedType(couponUsedType)
                .couponId(reservation.getCouponId())
                .paymentStatus(reservation.getPaymentStatus())
                .paymentType(reservation.getPaymentType())
                .createdAt(reservation.getCreatedAt())
                .createdBy(reservation.getCreatedBy())
                .build();
    }
}