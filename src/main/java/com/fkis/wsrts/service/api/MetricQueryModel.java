package com.fkis.wsrts.service.api;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class MetricQueryModel {

  private List<String> sensorIds;
  private long fromTimestamp;
  private long toTimestamp;
  private String queryType;
  private Map<String, Double> results;
}
