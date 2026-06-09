package com.example.prizes.client;

import java.util.List;

public interface PrizeClient {

    public List<Prize> getPrizes();

    public int takePrize(String name);
}
