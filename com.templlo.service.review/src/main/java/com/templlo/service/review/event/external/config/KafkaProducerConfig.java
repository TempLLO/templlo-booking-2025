package com.templlo.service.review.event.external.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

		// 재시도 관련 설정
		configProps.put(ProducerConfig.RETRIES_CONFIG, 0);                     // 재시도 횟수 제한

		// 브로커 연결 재시도 설정
		configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000); // 재연결 시도 간격
		configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 1000); // 최대 재연결 시도 간격
		configProps.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 1000); // send() 메서드가 블록되는 최대 시간

		// 백그라운드 연결 관련 추가 설정
		configProps.put(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 1000); // 연결 유지 시간
		configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 1000);     // 전체 전송 타임아웃
		configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);      // 요청 타임아웃

		return new DefaultKafkaProducerFactory<>(configProps);
	}

}
