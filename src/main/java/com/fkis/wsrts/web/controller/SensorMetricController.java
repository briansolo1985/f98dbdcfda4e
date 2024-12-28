package com.fkis.wsrts.web.controller;

import com.fkis.wsrts.service.api.SensorMetricModel;
import com.fkis.wsrts.service.api.SensorMetricService;
import com.fkis.wsrts.web.api.Sensor;
import com.fkis.wsrts.web.api.SensorMetric;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
public class SensorMetricController {

  private final ModelMapper modelMapper;
  private final SensorMetricService sensorMetricService;

  @PostMapping("/sensors/{id}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Results are ok", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = Sensor.class))}),
      @ApiResponse(responseCode = "400", description = "Invalid request",
          content = @Content)})
  @Operation(summary = "Receive metrics sent by a sensor")
  public void receiveMetric(@NotNull @Parameter @PathVariable String id,
      @Parameter @RequestBody @Valid SensorMetric sensorMetric) {
    SensorMetricModel sensorMetricModel = modelMapper.map(sensorMetric, SensorMetricModel.class);
    sensorMetricService.receiveMetric(id, sensorMetricModel);
  }

  @GetMapping("/sensors")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Results are ok", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = List.class))})})
  @Operation(summary = "Returns all metrics")
  public List<SensorMetric> getAllSensors() {
    return sensorMetricService.getAllMetrics()
        .stream()
        .toList();
  }
}
