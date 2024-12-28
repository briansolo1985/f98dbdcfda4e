package com.fkis.wsrts.service.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FilterField {
  SENSOR_ID("sensorId"),
  TIMESTAMP("timestamp");

  private final String fieldName;
}
