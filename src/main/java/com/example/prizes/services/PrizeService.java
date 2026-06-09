package com.example.prizes.services;

import com.example.prizes.data.InventoryDao;
import com.example.prizes.data.PrizeDao;
import com.example.prizes.model.PrizeEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeService {

    private final PrizeDao prizeDao;
    private final InventoryDao inventoryDao;

    public PrizeService(PrizeDao prizeDao, InventoryDao inventoryDao) {
        this.prizeDao = prizeDao;
        this.inventoryDao = inventoryDao;
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

    public void deletePrize(String name) {
        String normalizedName = name.toLowerCase();

        int prizeAmount = inventoryDao.getAmountByName(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("Prize not found in inventory: " + name));

        if (prizeAmount != 0) {
            throw new IllegalStateException("Prize cannot be deleted because inventory amount is not 0");
        }

        PrizeEntity prizeEntity = prizeDao.findByPrizeName(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("Prize not found in prize list: " + name));

        prizeDao.delete(prizeEntity);
    }
}
