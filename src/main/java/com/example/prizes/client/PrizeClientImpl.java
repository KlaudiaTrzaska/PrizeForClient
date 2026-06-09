package com.example.prizes.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PrizeClientImpl implements PrizeClient{

    private final RestClient restClient;


    public PrizeClientImpl(@Value("${prizes.service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public List<Prize> getPrizes() {
        return restClient
                .get()
                .uri("/prizes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Prize>>() {});
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
