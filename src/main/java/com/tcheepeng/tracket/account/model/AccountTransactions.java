package com.tcheepeng.tracket.account.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Data;

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
  @Column(name = "amount")
  private BigDecimal amount;

  @Basic
  @Column(name = "transaction_type")
  @Enumerated(value = EnumType.STRING)
  private AccountTransactionType transactionType;

  @Basic
  @Column(name = "exchange_rate")
  private BigDecimal exchangeRate;
}
