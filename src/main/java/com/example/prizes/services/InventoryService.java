package com.example.prizes.services;

import com.example.prizes.data.InventoryDao;
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

}
