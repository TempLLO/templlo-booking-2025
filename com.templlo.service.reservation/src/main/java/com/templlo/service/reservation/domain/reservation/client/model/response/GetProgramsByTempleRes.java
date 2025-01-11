package com.templlo.service.reservation.domain.reservation.client.model.response;

import java.util.List;
import java.util.UUID;

public record GetProgramsByTempleRes(
    ProgramType type,
    UUID programId,
    Integer programFee
) {
    public static List<UUID> getProgramIds(List<GetProgramsByTempleRes> dtos) {
        return dtos.stream().map(GetProgramsByTempleRes::programId).toList();
    }
}
