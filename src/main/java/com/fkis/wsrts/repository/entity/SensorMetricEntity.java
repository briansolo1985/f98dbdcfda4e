package com.fkis.wsrts.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(
    name = "SENSOR_METRIC",
    indexes = {
        @Index(name = "idx_sensor_id", columnList = "sensorId")
    })
@Data
public class SensorMetricEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "ID")
  private long id;

  @Column(name = "SENSOR_ID", nullable = false)
  private String sensorId;

  @Column(name = "TEMPERATURE")
  private Double temperature;

  @Column(name = "HUMIDITY")
  private Double humidity;

  @Column(name = "WIND_SPEED")
  private Double windSpeed;

  @Column(name = "AIR_PRESSURE")
  private Double airPressure;

  @Column(name = "TIMESTAMP")
  private long timestamp;
}
