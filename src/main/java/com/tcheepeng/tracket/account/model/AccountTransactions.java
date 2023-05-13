package com.tcheepeng.tracket.account.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "account_transactions", schema = "tracket")
public class AccountTransactions {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private int id;

  @Basic
  @Column(name = "transaction_ts")
  private Timestamp transactionTs;

  @Basic
  @Column(name = "account_id_from")
  private int accountIdFrom;

  @Basic
  @Column(name = "account_id_to")
  private Integer accountIdTo;

  @Basic
  @Column(name = "amount_in_cents")
  private int amountInCents;

  @Basic
  @Column(name = "transaction_type")
  @Enumerated(value=EnumType.STRING)
  private AccountTransactionType transactionType;
}
