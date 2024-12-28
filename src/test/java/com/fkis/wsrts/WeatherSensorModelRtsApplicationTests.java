package com.fkis.wsrts;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fkis.wsrts.service.api.SensorMetricModel;
import com.fkis.wsrts.service.api.SensorMetricService;
import com.fkis.wsrts.service.api.SensorModel;
import com.fkis.wsrts.service.api.SensorService;
import com.fkis.wsrts.web.api.MetricQueryResponse;
import com.fkis.wsrts.web.api.Sensor;
import com.fkis.wsrts.web.api.SensorMetric;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class WeatherSensorModelRtsApplicationTests {

  private static final String SENSORS_PATH = "/sensors";
  private static final String METRICS_SENSORS_PATH_TEMPLATE = "/metrics/sensors/%s";
  private static final String QUERY_SENSORS_PATH_TEMPLATE =
      "/query?sensorIds={sensorIds}&fields={fields}&range={range}&rangeEnd={rangeEnd}";

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private SensorService sensorService;

  @Autowired
  private SensorMetricService sensorMetricService;

  @Test
  public void shouldRegisterSensor() {
    // given
    Sensor sensor = sensor();

    // when
    ResponseEntity<Sensor> response = testRestTemplate.postForEntity(SENSORS_PATH, sensor,
        Sensor.class);

    // then
    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertEquals(sensor, response.getBody());
    List<SensorModel> sensors = sensorService.getAll();
    assertEquals(1, sensors.size());
    assertEquals(sensor.getId(), sensors.getFirst().getId());
  }

  @Test
  public void shouldReceiveThreeMetrics() {
    // given
    SensorModel sensor = sensorModel();
    sensorService.create(sensor);

    SensorMetric metric1 = sensorMetric(1.0, 10L);
    SensorMetric metric2 = sensorMetric(2.0, 20L);
    SensorMetric metric3 = sensorMetric(3.0, 30L);

    // when
    List<ResponseEntity<Void>> results = Stream.of(metric1, metric2, metric3)
        .map(sensorMetric -> testRestTemplate.postForEntity(
            format(METRICS_SENSORS_PATH_TEMPLATE, sensor.getId()), sensorMetric, Void.class))
        .toList();

    // then
    results.forEach(response -> assertEquals(response.getStatusCode(), response.getStatusCode()));
    List<SensorMetric> sensors = sensorMetricService.getAllMetrics();
    assertEquals(3, sensors.size());
  }

  @Test
  public void shouldGetLatestMetricWhenRangeIsNotProvided() {
    // given
    long baseTimestamp = Instant.now().toEpochMilli();

    SensorModel sensor = sensorModel();
    sensorService.create(sensor);
    SensorMetricModel metric1 = sensorMetricModel(1.0, baseTimestamp + 10L);
    SensorMetricModel metric2 = sensorMetricModel(2.0, baseTimestamp + 20L);
    SensorMetricModel metric3 = sensorMetricModel(3.0, baseTimestamp + 30L);
    Stream.of(metric1, metric2, metric3)
        .forEach(metric -> sensorMetricService.receiveMetric(sensor.getId(), metric));

    // when
    Map<String, ?> parameters = Map.of(
        "sensorIds", sensor.getId(),
        "fields", "",
        "range", "",
        "rangeEnd", ""
    );
    ResponseEntity<MetricQueryResponse> response = testRestTemplate.getForEntity(
        format(QUERY_SENSORS_PATH_TEMPLATE, sensor.getId()), MetricQueryResponse.class, parameters);

    // then
    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    MetricQueryResponse metricQueryResponse = response.getBody();
    assertNotNull(metricQueryResponse);
    assertEquals("LATEST", metricQueryResponse.getQueryType());
    assertTrue(metricQueryResponse.getResults().values().stream().allMatch(value -> value == 3.0));
  }

  @Test
  public void shouldCalculateAverageWhenRangeIsProvided() {
    // given
    long baseTimestamp = Instant.now().toEpochMilli();

    SensorModel sensor = sensorModel();
    sensorService.create(sensor);
    SensorMetricModel metric1 = sensorMetricModel(1.0, baseTimestamp + 10L);
    SensorMetricModel metric2 = sensorMetricModel(2.0, baseTimestamp + 20L);
    SensorMetricModel metric3 = sensorMetricModel(3.0, baseTimestamp + 30L);
    Stream.of(metric1, metric2, metric3)
        .forEach(metric -> sensorMetricService.receiveMetric(sensor.getId(), metric));

    // when
    Map<String, ?> parameters = Map.of(
        "sensorIds", "",
        "fields", "",
        "range", "PT5M",
        "rangeEnd", baseTimestamp + 60L
    );
    ResponseEntity<MetricQueryResponse> response = testRestTemplate.getForEntity(
        format(QUERY_SENSORS_PATH_TEMPLATE, sensor.getId()), MetricQueryResponse.class, parameters);

    // then
    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    MetricQueryResponse metricQueryResponse = response.getBody();
    assertNotNull(metricQueryResponse);
    assertEquals("AVERAGE", metricQueryResponse.getQueryType());
    assertEquals(4, metricQueryResponse.getResults().keySet().size());
    assertTrue(metricQueryResponse.getResults().values().stream().allMatch(value -> value == 2.0));
  }

  @Test
  public void shouldCalculateAverageWhenRangeIsProvidedOnlyForRequestedFields() {
    // given
    long baseTimestamp = Instant.now().toEpochMilli();

    SensorModel sensor = sensorModel();
    sensorService.create(sensor);
    SensorMetricModel metric1 = sensorMetricModel(1.0, baseTimestamp + 10L);
    SensorMetricModel metric2 = sensorMetricModel(2.0, baseTimestamp + 20L);
    SensorMetricModel metric3 = sensorMetricModel(3.0, baseTimestamp + 30L);
    Stream.of(metric1, metric2, metric3)
        .forEach(metric -> sensorMetricService.receiveMetric(sensor.getId(), metric));

    // when
    Map<String, ?> parameters = Map.of(
        "sensorIds", sensor.getId(),
        "fields", "temperature",
        "range", "PT5M",
        "rangeEnd", baseTimestamp + 60L
    );
    ResponseEntity<MetricQueryResponse> response = testRestTemplate.getForEntity(
        QUERY_SENSORS_PATH_TEMPLATE, MetricQueryResponse.class, parameters);

    // then
    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    MetricQueryResponse metricQueryResponse = response.getBody();
    assertNotNull(metricQueryResponse);
    assertEquals("AVERAGE", metricQueryResponse.getQueryType());
    Map<String, Double> results = metricQueryResponse.getResults();
    assertEquals(1, results.keySet().size());
    assertTrue(results.containsKey("temperature"));
    assertTrue(results.values().stream().allMatch(value -> value == 2.0));
  }


  private Sensor sensor() {
    Sensor sensor = new Sensor();
    sensor.setId("sensor1");
    sensor.setCountry("US");
    sensor.setCountry("SF");
    return sensor;
  }

  private SensorModel sensorModel() {
    SensorModel sensor = new SensorModel();
    sensor.setId("sensor1");
    sensor.setCountry("US");
    sensor.setCountry("SF");
    return sensor;
  }

  private SensorMetric sensorMetric(double sameValueForAllDimensions, long timestamp) {
    SensorMetric metric = new SensorMetric();
    metric.setAirPressure(sameValueForAllDimensions);
    metric.setHumidity(sameValueForAllDimensions);
    metric.setTemperature(sameValueForAllDimensions);
    metric.setWindSpeed(sameValueForAllDimensions);
    metric.setTimestamp(timestamp);
    return metric;
  }

  private SensorMetricModel sensorMetricModel(double sameValueForAllDimensions, long timestamp) {
    SensorMetricModel metric = new SensorMetricModel();
    metric.setAirPressure(sameValueForAllDimensions);
    metric.setHumidity(sameValueForAllDimensions);
    metric.setTemperature(sameValueForAllDimensions);
    metric.setWindSpeed(sameValueForAllDimensions);
    metric.setTimestamp(timestamp);
    return metric;
  }
}
