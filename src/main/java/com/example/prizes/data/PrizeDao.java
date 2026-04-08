package com.example.prizes.data;

import com.example.prizes.model.Prize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrizeDao extends JpaRepository<Prize, String> {


}
