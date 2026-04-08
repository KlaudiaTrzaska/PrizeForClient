package com.example.prizes.services;

import com.example.prizes.data.PrizeDao;
import com.example.prizes.model.Prize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeService {

    private final PrizeDao prizeDao;

    public PrizeService(PrizeDao prizeDao) {
        this.prizeDao = prizeDao;
    }

    public void addPrize(String name) {
        Prize prize = new Prize();
        prize.setPrizeName(name);
        prizeDao.save(prize);
    }

    public List<Prize> getAllPrizes() {
        return prizeDao.findAll();
    }
}
