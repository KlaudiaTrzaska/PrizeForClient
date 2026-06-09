package com.example.prizes.data;

import com.example.prizes.model.PrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrizeDao extends JpaRepository<PrizeEntity, String> {

    Optional<PrizeEntity> findByPrizeName(String prizeName);

}
