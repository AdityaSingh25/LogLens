package com.loglens.ingestion.service;

import com.loglens.ingestion.api.dto.IngestLogRequest;
import com.loglens.ingestion.kafka.RawLogProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private RawLogProducer producer;

    private IngestionServiceImpl ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new IngestionServiceImpl(producer);
        when(producer.publish(any())).thenReturn(Mono.empty());
    }

    @Test
    void ingest_validRequest_returnsLogId() {
        IngestLogRequest request = new IngestLogRequest();
        request.setServiceName("payment-service");
        request.setLevel("ERROR");
        request.setMessage("Payment gateway timeout");

        StepVerifier.create(ingestionService.ingest(request, "tenant-123"))
                .assertNext(response -> {
                    assertThat(response.getLogId()).isNotBlank();
                    assertThat(response.getAcceptedAt()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void ingest_defaultsTimestampWhenNotProvided() {
        IngestLogRequest request = new IngestLogRequest();
        request.setServiceName("auth-service");
        request.setLevel("INFO");
        request.setMessage("User logged in");
        // no timestamp set

        StepVerifier.create(ingestionService.ingest(request, "tenant-123"))
                .assertNext(response -> assertThat(response.getLogId()).isNotBlank())
                .verifyComplete();
    }
}
