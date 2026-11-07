package com.helpflow.dispatch.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cuerpo de la petición POST /technician-dispatch.
 * Ejemplo: { "ticketId": 125, "categoria": "RED", "prioridad": "ALTA" }
 */
public record DispatchRequest(
        @NotNull @Positive Long ticketId,
        @NotNull Categoria categoria,
        @NotNull Prioridad prioridad
) {
    public enum Categoria { HARDWARE, SOFTWARE, RED, ACCESOS, OTRO }

    public enum Prioridad { BAJA, MEDIA, ALTA, CRITICA }
}
