package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.service.DispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DispatchController.class)
@Import(DispatchService.class)
class DispatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void respondeConTecnicoYDisponibilidad() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticketId": 125, "categoria": "RED", "prioridad": "ALTA"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(125))
                .andExpect(jsonPath("$.tecnicoId").exists())
                .andExpect(jsonPath("$.disponibilidad").exists())
                .andExpect(jsonPath("$.tiempoEstimadoHoras").isNumber());
    }

    @Test
    void sinTicketId_responde400() throws Exception {
        mockMvc.perform(post("/technician-dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria": "RED", "prioridad": "ALTA"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
