package com.example.prizes.client;

import java.util.ArrayList;
import java.util.List;

public interface PrizeClient {

    int DEFAULT_PAGE_SIZE = 1000;

    default List<Prize> getPrizes() {
        List<Prize> prizes = new ArrayList<>();
        int page = 0;
        PrizePage prizePage;

        do {
            prizePage = getPrizes(page, DEFAULT_PAGE_SIZE);
            prizes.addAll(prizePage.content());
            page++;
        } while (!prizePage.last());

        return prizes;
    }

    PrizePage getPrizes(int page, int size);

    int takePrize(String name);
}
