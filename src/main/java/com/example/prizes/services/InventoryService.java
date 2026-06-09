package com.example.prizes.services;

import com.example.prizes.data.InventoryDao;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryDao inventoryDao;

    public InventoryService(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    public Optional<Integer> getAmountByName(String name) {
        return inventoryDao.getAmountByName(name.toLowerCase());
    }

    @Transactional
    public int takePrize(String name) {
        String normalizedName = name.toLowerCase();
        int updatedRows = inventoryDao.decreaseAmountByPrizeName(normalizedName);

        if (updatedRows == 0) {
            int prizeAmount = inventoryDao.getAmountByName(normalizedName)
                    .orElseThrow(() -> new IllegalArgumentException("Prize not found in inventory: " + name));

            if (prizeAmount == 0) {
                throw new IllegalStateException("Prize is out of stock: " + name);
            }
        }

        return inventoryDao.getAmountByName(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("Prize not found in inventory: " + name));
    }

}
