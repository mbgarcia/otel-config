package com.otel.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@RestController
@RequestMapping("/otel")
public class OtelController {

    private final Tracer tracer;
    private final LongCounter requestCounter;

    public OtelController(Tracer tracer, Meter meter) {
        this.tracer = tracer;

        this.requestCounter = meter
                .counterBuilder("otel_tester_requests_total")
                .setDescription("Total de chamadas no endpoint /ok")
                .build();
    }

    @GetMapping("/ok")
    public String ok() {

        Span span = tracer.spanBuilder("process-ok-endpoint").startSpan();

        try {
            requestCounter.add(1);

            span.setAttribute("endpoint", "/ok");
            span.setAttribute("custom.status", "executando");

            Thread.sleep(50);

            span.setAttribute("custom.status", "finalizado");

            return "ok";

        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("error", true);
            throw new RuntimeException(e);

        } finally {
            span.end();
        }
    }
}
