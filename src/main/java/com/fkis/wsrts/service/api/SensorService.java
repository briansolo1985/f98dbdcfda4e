package com.fkis.wsrts.service.api;

import java.util.List;

public interface SensorService {

  SensorModel create(SensorModel sensorModel);

  List<SensorModel> getAll();
}
