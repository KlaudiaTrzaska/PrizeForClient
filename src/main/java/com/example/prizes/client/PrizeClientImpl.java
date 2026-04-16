package com.example.prizes.client;

import com.example.prizes.client.dto.Prize;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class PrizeClientImpl implements PrizeClient {

    private final RestClient restClient;

    public PrizeClientImpl(String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
    @Override
    public List<Prize> getAllPrizes() {
        return restClient.get()
                .uri("/prizes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Prize>>() {});
    }
}
