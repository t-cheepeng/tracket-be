package com.tcheepeng.tracket.group.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Table(name = "account_group", schema = "tracket")
public class AccountGroup {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name="name", unique = true)
    private String name;

    @Column(name="currency")
    private String currency;
}
