package com.example.prizes.services;

import com.example.prizes.client.dto.Prize;
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

    public void addPrize(String name) {
        PrizeEntity prizeEntity = new PrizeEntity();
        prizeEntity.setPrizeName(name);
        prizeDao.save(prizeEntity);
    }

    public List<Prize> getAllPrizes() {
        return prizeDao.findAll().stream().map(entity -> new Prize(entity.getPrizeName(), entity.getThreshold())).toList();
    }
}
