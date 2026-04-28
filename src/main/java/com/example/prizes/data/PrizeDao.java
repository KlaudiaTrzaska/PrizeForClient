package com.example.prizes.data;

import com.example.prizes.model.PrizeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PrizeDao extends JpaRepository<PrizeEntity, String> {

}
