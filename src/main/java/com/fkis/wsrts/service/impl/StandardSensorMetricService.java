package com.fkis.wsrts.service.impl;

import static java.time.Instant.now;

import com.fkis.wsrts.repository.SensorMetricRepository;
import com.fkis.wsrts.repository.SensorRepository;
import com.fkis.wsrts.repository.entity.SensorMetricEntity;
import com.fkis.wsrts.service.api.SensorMetricModel;
import com.fkis.wsrts.service.api.SensorMetricService;
import com.fkis.wsrts.web.api.SensorMetric;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class StandardSensorMetricService implements SensorMetricService {

  private final SensorRepository sensorRepository;
  private final SensorMetricRepository sensorMetricRepository;
  private final ModelMapper modelMapper;

  @Override
  public void receiveMetric(String sensorId, SensorMetricModel sensorMetricModel) {
    sensorRepository.findById(sensorId)
        .orElseThrow(() -> new IllegalStateException("Unknown sensor, please register it first"));

    SensorMetricEntity sensorMetricEntity = modelMapper.map(sensorMetricModel,
        SensorMetricEntity.class);
    sensorMetricEntity.setSensorId(sensorId);
    if (sensorMetricEntity.getTimestamp() == 0L) {
      sensorMetricEntity.setTimestamp(now().toEpochMilli());
    }
    sensorMetricRepository.save(sensorMetricEntity);
  }

  @Override
  public List<SensorMetric> getAllMetrics() {
    return sensorMetricRepository.findAll()
        .stream()
        .map(sensorMetricEntity -> modelMapper.map(sensorMetricEntity, SensorMetric.class))
        .toList();
  }
}
