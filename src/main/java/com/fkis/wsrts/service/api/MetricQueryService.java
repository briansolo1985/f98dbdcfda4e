package com.fkis.wsrts.service.api;

import java.time.Duration;
import java.util.List;

public interface MetricQueryService {

  MetricQueryModel fetchLatestMetric(List<String> sensorIds, List<MetricField> fields);

  MetricQueryModel calculateAverageMetrics(List<String> sensorIds, List<MetricField> fields, Duration range,
      Long toTimestamp);
}
