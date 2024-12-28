package com.fkis.wsrts.service.api;

import com.fkis.wsrts.web.api.SensorMetric;
import java.util.List;

public interface SensorMetricService {

  void receiveMetric(String sensorId, SensorMetricModel sensorMetricModel);

  List<SensorMetric> getAllMetrics();
}
