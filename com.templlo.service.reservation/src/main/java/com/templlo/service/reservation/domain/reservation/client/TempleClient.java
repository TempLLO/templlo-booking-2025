package com.templlo.service.reservation.domain.reservation.client;

import com.templlo.service.reservation.domain.reservation.client.model.response.GetProgramsByTempleRes;
import com.templlo.service.reservation.domain.reservation.client.model.response.ProgramServiceWrapperRes;
import com.templlo.service.reservation.global.feign.AuthHeader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;
import java.util.UUID;

@FeignClient("program-service")
public interface TempleClient {

    @AuthHeader
    @GetMapping("/api/programs/temples/{templeId}")
    ProgramServiceWrapperRes<List<GetProgramsByTempleRes>> getProgramsByTemple(@PathVariable(name = "templeId") UUID templeId);
}
