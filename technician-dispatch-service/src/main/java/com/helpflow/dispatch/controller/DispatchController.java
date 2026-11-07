package com.helpflow.dispatch.controller;

import com.helpflow.dispatch.dto.DispatchRequest;
import com.helpflow.dispatch.dto.DispatchResponse;
import com.helpflow.dispatch.service.DispatchService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/technician-dispatch")
public class DispatchController {

    private static final Logger log = LoggerFactory.getLogger(DispatchController.class);

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping
    public DispatchResponse despachar(@Valid @RequestBody DispatchRequest request) {
        DispatchResponse respuesta = dispatchService.despachar(request);
        log.info("Dispatch ticketId={} categoria={} prioridad={} -> tecnico={} horas={}",
                request.ticketId(), request.categoria(), request.prioridad(),
                respuesta.tecnicoId(), respuesta.tiempoEstimadoHoras());
        return respuesta;
    }
}
