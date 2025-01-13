package com.templlo.service.program.kafka.message.reservation;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationCreateMessage(
        UUID userId,
        UUID reservationId,
        UUID programId,
        LocalDate programDate,
        ReservationOpenType openType,
        Gender gender,
        Integer amount,
        CouponUsedType couponUsedType,
        UUID couponId
) {
}
