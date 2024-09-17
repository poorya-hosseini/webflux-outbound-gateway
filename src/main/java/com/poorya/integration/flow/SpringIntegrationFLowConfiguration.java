package com.poorya.integration.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableIntegration
@Slf4j
public class SpringIntegrationFLowConfiguration {

    @Bean
    public IntegrationFlow inboundPost(
            @Qualifier("inputChannel") MessageChannel inputChannel) {

        return IntegrationFlow
                .from(WebFlux.inboundGateway("/reactivePost")
                        .requestMapping(requestMapping -> requestMapping.methods(HttpMethod.POST))
                        .requestPayloadType(String.class)
                        .statusCodeFunction(m -> HttpStatus.OK))
                .channel(inputChannel)
                .get();
    }

    @Bean("inputChannel")
    public PublishSubscribeChannel inputChannel() {
        PublishSubscribeChannel channel = new PublishSubscribeChannel();
        channel.setDatatypes(String.class);
        return channel;
    }

    @Bean("outputGet")
    public IntegrationFlow outboundChannelFlow(
            @Qualifier("inputChannel") MessageChannel inputChannel) {

        return IntegrationFlow
                .from(inputChannel)
                .handle(WebFlux.<String>outboundGateway(message ->
                                UriComponentsBuilder.fromUriString("http://localhost:8080/")
                                        .pathSegment(message.getPayload())
                                        .build()
                                        .toUri())
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(String.class))
                .get();
    }
}
