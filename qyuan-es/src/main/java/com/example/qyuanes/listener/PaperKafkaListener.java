package com.example.qyuanes.listener;
import com.example.qyuanes.entity.Paper;
import com.example.qyuanes.service.ElasticSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaperKafkaListener {

    private final ObjectMapper objectMapper;
    private final ElasticSearchService elasticSearchService;
}
