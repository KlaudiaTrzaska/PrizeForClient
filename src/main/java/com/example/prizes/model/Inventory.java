package com.example.prizes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @Getter
    @Setter
    private String id;

    @Column(name = "prize_amount")
    @Getter
    @Setter
    private int prizeAmount;
}

