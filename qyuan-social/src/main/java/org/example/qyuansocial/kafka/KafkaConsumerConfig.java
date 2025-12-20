package org.example.qyuansocial.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 消费者公共配置
 *
 * 说明：
 * - bootstrapServers、groupId 从配置文件 / Nacos 中注入；
 * - 统一设置反序列化为 String，方便后续用 fastjson2 / Jackson 自己解析 JSON。
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    /**
     * Kafka 集群地址，例如：127.0.0.1:9092
     *
     * 推荐在 Nacos / 本地配置中设置：
     * spring.kafka.bootstrap-servers=127.0.0.1:9092
     */
    @Value("${spring.kafka.bootstrap-servers:127.0.0.1:9092}")
    private String bootstrapServers;

    /**
     * 默认消费者组
     *
     * 推荐在配置中设置：
     * spring.kafka.consumer.group-id=qyuan-social-group
     */
    @Value("${spring.kafka.consumer.group-id:qyuan-social-group}")
    private String groupId;

    /**
     * 构建消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 自动提交 offset，开发环境简单使用；生产可以改为手动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // latest：只消费启动之后的新消息；如果希望从头消费可改为 earliest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 供 @KafkaListener 使用的监听容器工厂
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // 可以设置并发度，默认 1；根据分区数和负载情况调整
        factory.setConcurrency(1);
        return factory;
    }
}


