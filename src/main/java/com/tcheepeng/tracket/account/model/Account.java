package com.tcheepeng.tracket.account.model;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Entity
@Table(name = "account", schema = "tracket")
public class Account {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private int id;

  @Basic
  @Column(name = "name")
  private String name;

  @Basic
  @Column(name = "currency")
  private String currency;

  @Basic
  @Column(name = "creation_ts")
  private Timestamp creationTs;

  @Basic
  @Column(name = "account_type")
  @Enumerated(EnumType.STRING)
  private AccountType accountType;

  @Basic
  @Column(name = "description")
  private String description;

  @Basic
  @Column(name = "cash_in_cents")
  private int cashInCents;

  @Basic
  @Column(name = "is_deleted")
  private boolean isDeleted;
}
