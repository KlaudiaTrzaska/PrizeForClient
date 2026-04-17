package com.example.prizes.data;

import com.example.prizes.model.PrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeDao extends JpaRepository<PrizeEntity, String> {


}
