package com.fkis.wsrts.service.impl;

import com.fkis.wsrts.repository.SensorRepository;
import com.fkis.wsrts.repository.entity.SensorEntity;
import com.fkis.wsrts.service.api.SensorModel;
import com.fkis.wsrts.service.api.SensorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(isolation = Isolation.READ_COMMITTED)
@Service
@RequiredArgsConstructor
public class StandardSensorService implements SensorService {

  private final SensorRepository sensorRepository;
  private final ModelMapper modelMapper;

  @Override
  public SensorModel create(SensorModel sensorModel) {
    sensorRepository.findById(sensorModel.getId())
        .ifPresent(sensor -> {
          throw new IllegalStateException("Sensor is already registered with id " + sensor.getId());
        });

    SensorEntity newSensor = modelMapper.map(sensorModel, SensorEntity.class);
    SensorEntity createdSensor = sensorRepository.save(newSensor);
    log.debug("Created sensor {}", createdSensor);
    return convert(createdSensor);
  }

  @Override
  public List<SensorModel> getAll() {
    return sensorRepository.findAll()
        .stream()
        .map(this::convert)
        .toList();
  }

  private SensorModel convert(SensorEntity sensorEntity) {
    return modelMapper.map(sensorEntity, SensorModel.class);
  }
}
