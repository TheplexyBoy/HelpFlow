package com.helpflow.dispatch.service;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchRequest.Categoria;
import com.helpflow.dispatch.dto.DispatchRequest.Prioridad;
import com.helpflow.dispatch.dto.DispatchResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchServiceTest {

    private final DispatchService service = new DispatchService();

    @Test
    void mismaCombinacion_devuelveSiempreElMismoTecnico() {
        DispatchRequest request = new DispatchRequest(125L, Categoria.RED, Prioridad.ALTA);

        DispatchResponse primera = service.despachar(request);
        DispatchResponse segunda = service.despachar(request);

        assertThat(segunda).isEqualTo(primera);
    }

    @Test
    void elTecnicoDevueltoEsDeLaEspecialidadSolicitada() {
        DispatchResponse respuesta = service.despachar(new DispatchRequest(7L, Categoria.RED, Prioridad.MEDIA));

        assertThat(respuesta.tecnicoId()).isIn(List.of("TEC-005", "TEC-006"));
    }

    @Test
    void clasificaLaDisponibilidadSegunLasHoras() {
        assertThat(DispatchService.clasificar(0)).isEqualTo("ALTA");
        assertThat(DispatchService.clasificar(2)).isEqualTo("ALTA");
        assertThat(DispatchService.clasificar(3)).isEqualTo("MEDIA");
        assertThat(DispatchService.clasificar(8)).isEqualTo("MEDIA");
        assertThat(DispatchService.clasificar(9)).isEqualTo("BAJA");
    }
}
