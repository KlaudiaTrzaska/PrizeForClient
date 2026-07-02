package com.example.prizes.data;

import com.example.prizes.model.PrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrizeDao extends JpaRepository<PrizeEntity, String> {

    Optional<PrizeEntity> findByPrizeName(String prizeName);

    @Query("SELECT p FROM PrizeEntity p WHERE p.threshold <= :threshold")
    List<PrizeEntity> getPrizesByThreshold(@Param("threshold") int threshold);
}
