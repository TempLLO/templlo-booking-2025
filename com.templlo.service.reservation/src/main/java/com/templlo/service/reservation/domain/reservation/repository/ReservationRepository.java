package com.templlo.service.reservation.domain.reservation.repository;

import com.templlo.service.reservation.domain.reservation.controller.model.response.ReservationListRes;
import com.templlo.service.reservation.domain.reservation.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.PagedModel;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Page<Reservation> findAllByUserId(UUID userId, Pageable pageable);

    default PagedModel<ReservationListRes> findAllByUserIdOfPagedModel(UUID userId, Pageable pageable) {
        Page<Reservation> pageEntity = findAllByUserId(userId, pageable);
        Page<ReservationListRes> pageDto = pageEntity.map(ReservationListRes::from);
        return new PagedModel<>(pageDto);
    }
}