package com.tcheepeng.tracket.external.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "sgx_fetch_price_history", schema = "tracket", catalog = "tracket")
public class SgxFetchPriceHistory {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "id")
  private int id;

  @Basic
  @Column(name = "path_code")
  private int pathCode;

  @Basic
  @Column(name = "date_of_path_code")
  private Date dateOfPathCode;

  @Basic
  @Column(name = "fetch_ts")
  private Timestamp fetchTs;

  @Basic
  @Column(name = "file_blob")
  private byte[] fileBlob;
}
