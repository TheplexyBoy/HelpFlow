package com.helpflow.dispatch.dto;

/**
 * Respuesta: { "ticketId": 125, "tecnicoId": "TEC-014", "disponibilidad": "MEDIA", "tiempoEstimadoHoras": 4 }
 */
public record DispatchResponse(
        Long ticketId,
        String tecnicoId,
        String disponibilidad,
        int tiempoEstimadoHoras
) {
}
