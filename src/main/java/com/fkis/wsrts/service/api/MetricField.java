package com.fkis.wsrts.service.api;

import static java.util.Arrays.stream;

import com.fkis.wsrts.repository.entity.SensorMetricEntity;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MetricField {

  TEMPERATURE(SensorMetricEntity::getTemperature, "temperature"),
  HUMIDITY(SensorMetricEntity::getHumidity, "humidity"),
  WIND_SPEED(SensorMetricEntity::getWindSpeed, "windSpeed"),
  AIR_PRESSURE(SensorMetricEntity::getAirPressure, "airPressure");

  private final Function<SensorMetricEntity, Double> extractor;
  private final String fieldName;

  public static Optional<MetricField> forName(String name) {
    return stream(values())
        .filter(metricField -> metricField.getFieldName().equalsIgnoreCase(name))
        .findFirst();
  }

  public Double extract(SensorMetricEntity sensorMetricEntity) {
    return extractor.apply(sensorMetricEntity);
  }
}
