package com.example.prizes.controllers;

import com.example.prizes.client.Prize;
import com.example.prizes.services.PrizeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class PrizeController {

    PrizeService prizeService;

    public PrizeController(PrizeService prizeService) {
        this.prizeService = prizeService;
    }

    @GetMapping("/addPrize")
    public String addPrize(@RequestParam String prizeName) {
        prizeService.addPrize(prizeName);
        return "Prize " + prizeName + " added!";
    }

    @GetMapping("/prizes")
    public List<Prize> getPrizes() {
        return prizeService.getAllPrizes()
                .stream()
                .map(entity ->
                        new Prize(entity.getPrizeName(), entity.getThreshold())
                ).toList();
    }
}
