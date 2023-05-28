package com.tcheepeng.tracket.account.model;

import com.tcheepeng.tracket.stock.model.AssetClass;

public interface StockOwned {
  String getStockName();

  String getCurrency();

  AssetClass getAssetClass();

  Integer getNumOfUnitsHeld();

  Integer getTotalFee();

  Integer getCostBasis();
}
