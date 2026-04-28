package com.example.prizes.services;

import com.example.prizes.data.PrizeDao;
import com.example.prizes.model.PrizeEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeService {

    private final PrizeDao prizeDao;

    public PrizeService(PrizeDao prizeDao) {
        this.prizeDao = prizeDao;
    }

    public void addPrize(String name, int threshold) {
        PrizeEntity prizeEntity = new PrizeEntity();
        prizeEntity.setPrizeName(name.toLowerCase());
        prizeEntity.setThreshold(threshold);
        prizeDao.save(prizeEntity);
    }

    public List<PrizeEntity> getAllPrizes() {
        return prizeDao.findAll();
    }
}
