package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Simula la disponibilidad de técnicos de forma DETERMINISTA:
 * la misma combinación (ticketId + categoría + prioridad) siempre devuelve
 * el mismo técnico y el mismo tiempo estimado.
 */
@Service
public class DispatchService {

    /**
     * Catálogo simulado de técnicos por especialidad.
     * Los códigos coinciden con los técnicos sembrados en la base de datos
     * del support-ticket-service (V3__datos_iniciales.sql).
     */
    private static final Map<DispatchRequest.Categoria, List<String>> TECNICOS_POR_CATEGORIA = Map.of(
            DispatchRequest.Categoria.HARDWARE, List.of("TEC-001", "TEC-002"),
            DispatchRequest.Categoria.SOFTWARE, List.of("TEC-003", "TEC-004"),
            DispatchRequest.Categoria.RED, List.of("TEC-005", "TEC-006"),
            DispatchRequest.Categoria.ACCESOS, List.of("TEC-007", "TEC-008"),
            DispatchRequest.Categoria.OTRO, List.of("TEC-009", "TEC-010")
    );

    public DispatchResponse despachar(DispatchRequest request) {
        // 1. Convertir ticketId + categoría + prioridad en un seed numérico.
        //    Objects.hash siempre da el mismo número para los mismos valores.
        long seed = Objects.hash(request.ticketId(), request.categoria().name(), request.prioridad().name());

        // 2. Random con seed fijo => la secuencia de "aleatorios" siempre es la misma.
        Random random = new Random(seed);

        List<String> candidatos = TECNICOS_POR_CATEGORIA.get(request.categoria());
        String tecnico = candidatos.get(random.nextInt(candidatos.size()));

        int horas = calcularHoras(request.prioridad(), random);

        // 3. Clasificar el tiempo estimado
        return new DispatchResponse(request.ticketId(), tecnico, clasificar(horas), horas);
    }

    /** Rango de horas posible según la prioridad (más prioridad = menos espera). */
    private int calcularHoras(DispatchRequest.Prioridad prioridad, Random random) {
        int[] rango = switch (prioridad) {
            case CRITICA -> new int[]{0, 3};
            case ALTA -> new int[]{1, 6};
            case MEDIA -> new int[]{2, 12};
            case BAJA -> new int[]{4, 24};
        };
        return rango[0] + random.nextInt(rango[1] - rango[0] + 1);
    }

    /** 0–2 h → ALTA, 3–8 h → MEDIA, más de 8 h → BAJA. */
    static String clasificar(int horas) {
        if (horas <= 2) {
            return "ALTA";
        }
        if (horas <= 8) {
            return "MEDIA";
        }
        return "BAJA";
    }
}
