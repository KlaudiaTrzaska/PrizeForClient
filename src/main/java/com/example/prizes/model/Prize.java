package com.example.prizes.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "prize_name")
    @Getter
    @Setter
    private String prizeName;

    @Column
    @Getter
    @Setter
    private int threshold;



}
