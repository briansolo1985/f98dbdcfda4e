package com.fkis.wsrts.service.impl;

import static java.time.Instant.now;
import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.fkis.wsrts.repository.DynamicMetricRepository;
import com.fkis.wsrts.repository.SensorMetricRepository;
import com.fkis.wsrts.repository.entity.SensorMetricEntity;
import com.fkis.wsrts.service.api.MetricField;
import com.fkis.wsrts.service.api.MetricQueryModel;
import com.fkis.wsrts.service.api.MetricQueryService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class StandardMetricQueryService implements MetricQueryService {

  private static final String LATEST_METRIC_QUERY_TYPE = "LATEST";
  private static final String AVERAGE_QUERY_TYPE = "AVERAGE";

  private final SensorMetricRepository sensorMetricRepository;
  private final DynamicMetricRepository dynamicMetricRepository;

  @Override
  public MetricQueryModel fetchLatestMetric(List<String> sensorIds, List<MetricField> fields) {
    Optional<SensorMetricEntity> result = sensorIds.isEmpty()
        ? sensorMetricRepository.findFirstByOrderByTimestampDesc()
        : sensorMetricRepository.findFirstBySensorIdInOrderByTimestampDesc(sensorIds);

    return result
        .map(sensorMetricEntity -> {
          Map<String, Double> values = fields.stream()
              .collect(toMap(
                  MetricField::getFieldName,
                  field -> ofNullable(field.extract(sensorMetricEntity)).orElse(Double.NaN)));
          return metricQueryModel(sensorIds, LATEST_METRIC_QUERY_TYPE, values,
              sensorMetricEntity.getTimestamp(), sensorMetricEntity.getTimestamp());
        })
        .orElseThrow(
            () -> new IllegalArgumentException(
                "Unable to find metrics for sensor(s): " + sensorIds));
  }

  @Override
  public MetricQueryModel calculateAverageMetrics(List<String> sensorIds, List<MetricField> fields,
      Duration range, Long rangeEnd) {
    long toTimestamp = ofNullable(rangeEnd).map(Instant::ofEpochMilli).orElse(now()).toEpochMilli();
    long fromTimestamp = ofEpochMilli(toTimestamp).minus(range).toEpochMilli();

    Map<String, Double> averages = dynamicMetricRepository.calculateAverages(
        sensorIds, fields, fromTimestamp, toTimestamp);

    return metricQueryModel(sensorIds, AVERAGE_QUERY_TYPE, averages, fromTimestamp, toTimestamp);
  }

  private static MetricQueryModel metricQueryModel(List<String> sensorIds, String queryType,
      Map<String, Double> averages, long fromTimestamp, long toTimestamp) {
    MetricQueryModel metricQueryModel = new MetricQueryModel();
    metricQueryModel.setSensorIds(sensorIds);
    metricQueryModel.setQueryType(queryType);
    metricQueryModel.setResults(averages);
    metricQueryModel.setFromTimestamp(fromTimestamp);
    metricQueryModel.setToTimestamp(toTimestamp);
    return metricQueryModel;
  }
}
