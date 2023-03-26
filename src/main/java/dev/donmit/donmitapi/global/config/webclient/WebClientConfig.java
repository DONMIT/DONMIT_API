package dev.donmit.donmitapi.global.config.webclient;

import static dev.donmit.donmitapi.global.common.Constants.*;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.LoggingCodecSupport;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Configuration
@Slf4j
public class WebClientConfig {

	@Bean
	public WebClient gitHubWebClient() {
		// in-memory buffer 값 설정
		ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
			.build();

		// request, response 정보 로깅
		exchangeStrategies
			.messageWriters().stream()
			.filter(LoggingCodecSupport.class::isInstance)
			.forEach(writer -> ((LoggingCodecSupport)writer).setEnableLoggingRequestDetails(true));

		// WebClient Timeout 설정 (5초)
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
			.responseTimeout(Duration.ofMillis(RESPONSE_TIMEOUT))
			.doOnConnected(conn ->
				conn.addHandlerLast(new ReadTimeoutHandler(READ_TIME_OUT, TimeUnit.MILLISECONDS))
					.addHandlerLast(new WriteTimeoutHandler(WRITE_TIME_OUT, TimeUnit.MILLISECONDS)));

		return WebClient.builder()
			.exchangeStrategies(exchangeStrategies)
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}
}
