package com.example.prizes.data;

import com.example.prizes.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryDao extends JpaRepository<Inventory, String> {

    @Query("SELECT i.prizeAmount FROM Inventory i INNER JOIN PrizeEntity p ON i.id = p.id WHERE p.prizeName = :name")
    Optional<Integer> getAmountByName(@Param("name") String name);

    @Modifying
    @Query(value = """
            UPDATE inventory
            SET prize_amount = prize_amount - 1
            WHERE id = (SELECT id FROM prizes WHERE prize_name = :name)
              AND prize_amount > 0
            """, nativeQuery = true)
    int decreaseAmountByPrizeName(@Param("name") String name);

}
