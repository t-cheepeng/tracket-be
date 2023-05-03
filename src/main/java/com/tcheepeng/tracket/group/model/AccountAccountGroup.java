package com.tcheepeng.tracket.group.model;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "account_account_group", schema = "tracket")
public class AccountAccountGroup {

  @EmbeddedId private EmbeddedAccountAccountGroup accountAccountGroup;

  @Embeddable
  @Data
  public static class EmbeddedAccountAccountGroup implements Serializable {
    @Column(name = "account_id")
    private int accountId;

    @Column(name = "account_group_id")
    private int accountGroupId;
  }
}
