package com.demo;

import com.demo.api.RestClientService;
import com.demo.api.kafka.ConsumptionEventProducer;
import com.demo.api.model.Item;
import com.github.benmanes.caffeine.cache.Cache;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Controller("/items")
@Secured(SecurityRule.IS_ANONYMOUS)
public class ItemController {

    final RestClientService restClientService;
    final ConsumptionEventProducer consumptionEventProducer;
    final Cache<String, String> cache;

    public ItemController(RestClientService restClientService, ConsumptionEventProducer consumptionEventProducer, Cache<String, String> cache) {
        this.restClientService = restClientService;
        this.consumptionEventProducer = consumptionEventProducer;
        this.cache = cache;
    }

    @Get("/")
    public List<Item> listItems() {
        MutableHttpRequest<Item> request = HttpRequest.GET("https://random-data-api.com/api/address/random_address");
        Mono<String> response = restClientService.externalApiCall(request, UUID.randomUUID().toString(), String.class)
                .map(data -> {
                    consumptionEventProducer.sendKafkaEvent(UUID.randomUUID().toString());
                    System.out.println("Perform Some Function");
                    return data;
                });
        System.out.println(response);

        return List.of(
                new Item("Item 1"),
                new Item("Item 2"),
                new Item("Item 3")
        );
    }
}