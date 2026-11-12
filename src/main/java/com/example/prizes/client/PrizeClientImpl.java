package com.example.prizes.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PrizeClientImpl implements PrizeClient{

    private final RestClient restClient;


    public PrizeClientImpl(@Value("${prizes.service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public PrizePage getPrizes(int page, int size) {
        PrizePage prizes = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/prizes")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(PrizePage.class);

        if (prizes == null) {
            throw new IllegalStateException("Prize service did not return prizes");
        }

        return prizes;
    }

    @Override
    public int takePrize(String name) {
        Integer remainingAmount = restClient
                .post()
                .uri("/prizes/{name}/take", name)
                .retrieve()
                .body(Integer.class);

        if (remainingAmount == null) {
            throw new IllegalStateException("Prize service did not return remaining amount");
        }

        return remainingAmount;
    }
}
