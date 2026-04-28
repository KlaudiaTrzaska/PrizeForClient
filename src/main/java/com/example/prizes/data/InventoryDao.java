package com.example.prizes.data;

import com.example.prizes.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryDao extends JpaRepository<Inventory, String> {

    @Query("SELECT i.prizeAmount FROM Inventory i INNER JOIN PrizeEntity p ON i.id = p.id WHERE p.prizeName = :name")
    Optional<Integer> getAmountByName(String name);

}
