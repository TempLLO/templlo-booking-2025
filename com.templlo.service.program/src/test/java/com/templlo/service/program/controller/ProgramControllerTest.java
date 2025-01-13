package com.templlo.service.program.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.templlo.service.program.dto.request.CreateProgramRequest;
import com.templlo.service.program.entity.ProgramStatus;
import com.templlo.service.program.entity.ProgramType;
import com.templlo.service.program.exception.ProgramStatusCode;
import com.templlo.service.program.service.ProgramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {ProgramController.class})
class ProgramControllerTest {

    @Autowired
    private MockMvc modMvc;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public ProgramService programService() {
            return Mockito.mock(ProgramService.class);
        }
    }

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(authorities = "TEMPLE_ADMIN")
    void createProgram() throws Exception {

        //given
        CreateProgramRequest request = new CreateProgramRequest(
                UUID.randomUUID(),
                "제목",
                "설명",
                ProgramType.BLIND_DATE,
                0,
                LocalTime.parse("16:00"),
                LocalDate.parse("2025-10-10"),
                LocalDate.parse("2025-10-31"),
                ProgramStatus.ACTIVE,
                null,
                null,
                LocalDate.parse("2025-11-11"),
                50,
                50
        );

        String requestDtoString = objectMapper.writeValueAsString(request);

        // when
        MockHttpServletRequestBuilder requestPost = post("/api/programs")
                .content(requestDtoString)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf());

        ResultActions resultActions = modMvc.perform(requestPost);

        ProgramStatusCode code = ProgramStatusCode.SUCCESS_PROGRAM_CREATE;

        // then
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(code.getMessage()));

    }



    @Test
    void getProgramSchedule() {
    }
}