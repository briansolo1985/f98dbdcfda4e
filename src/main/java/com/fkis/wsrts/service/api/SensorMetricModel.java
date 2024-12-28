package com.fkis.wsrts.service.api;

import lombok.Data;

@Data
public class SensorMetricModel {

  private Double temperature;
  private Double humidity;
  private Double windSpeed;
  private Double airPressure;
  private long timestamp;
}
