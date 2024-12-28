package com.fkis.wsrts.service.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fkis.wsrts.repository.SensorRepository;
import com.fkis.wsrts.repository.entity.SensorEntity;
import com.fkis.wsrts.service.api.SensorModel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
public class StandardSensorMetricServiceTest {

  @Mock
  private SensorRepository mockSensorRepository;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private StandardSensorService testStandardSensorService;

  @Test
  public void shouldThrowExceptionWhenSensorIdIsAlreadyPresent() {
    // given
    String sensorId = "id1";
    SensorModel sensorModel = sensorModel(sensorId);

    when(mockSensorRepository.findById(sensorId)).thenReturn(of(sensorEntity(sensorId)));

    // when + then
    assertThrows(IllegalStateException.class, () -> testStandardSensorService.create(sensorModel));
  }

  @Test
  public void shouldCreateSensor() {
    // given
    String sensorId = "id1";
    String country = "US";
    String city = "NY";
    SensorModel inputSensorModel = sensorModel(sensorId, country, city);
    SensorEntity newSensorEntity = sensorEntity(sensorId, country, city);
    SensorEntity createdSensorEntity = sensorEntity(sensorId, country, city);
    SensorModel createdSensorModel = sensorModel(sensorId, country, city);

    when(mockSensorRepository.findById(sensorId)).thenReturn(empty());
    when(modelMapper.map(inputSensorModel, SensorEntity.class)).thenReturn(newSensorEntity);
    when(mockSensorRepository.save(newSensorEntity)).thenReturn(createdSensorEntity);
    when(modelMapper.map(createdSensorEntity, SensorModel.class)).thenReturn(createdSensorModel);

    // when
    SensorModel result = testStandardSensorService.create(inputSensorModel);

    // then
    assertEquals(inputSensorModel, result);
  }

  @Test
  public void shouldReturnAllSensors() {
    // given
    SensorEntity sensorEntity1 = sensorEntity("id1");
    SensorEntity sensorEntity2 = sensorEntity("id2");
    SensorEntity sensorEntity3 = sensorEntity("id3");
    List<SensorEntity> sensorEntities = List.of(sensorEntity1, sensorEntity2, sensorEntity3);
    SensorModel sensorModel1 = sensorModel("id1");
    SensorModel sensorModel2 = sensorModel("id2");
    SensorModel sensorModel3 = sensorModel("id3");
    List<SensorModel> sensorModels = List.of(sensorModel1, sensorModel2, sensorModel3);

    when(mockSensorRepository.findAll()).thenReturn(sensorEntities);
    when(modelMapper.map(sensorEntity1, SensorModel.class)).thenReturn(sensorModel1);
    when(modelMapper.map(sensorEntity2, SensorModel.class)).thenReturn(sensorModel2);
    when(modelMapper.map(sensorEntity3, SensorModel.class)).thenReturn(sensorModel3);

    // when
    List<SensorModel> results = testStandardSensorService.getAll();

    // then
    assertEquals(sensorModels, results);
  }

  private SensorModel sensorModel(String sensorId) {
    return sensorModel(sensorId, null, null);
  }

  private SensorModel sensorModel(String sensorId, String country, String city) {
    SensorModel sensorModel = new SensorModel();
    sensorModel.setId(sensorId);
    sensorModel.setCountry(country);
    sensorModel.setCity(city);
    return sensorModel;
  }

  private SensorEntity sensorEntity(String sensorId) {
    return sensorEntity(sensorId, null, null);
  }

  private SensorEntity sensorEntity(String sensorId, String country, String city) {
    SensorEntity sensorEntity = new SensorEntity();
    sensorEntity.setId(sensorId);
    sensorEntity.setCountry(country);
    sensorEntity.setCity(city);
    return sensorEntity;
  }
}
