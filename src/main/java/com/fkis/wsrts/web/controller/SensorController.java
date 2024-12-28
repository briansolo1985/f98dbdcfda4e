package com.fkis.wsrts.web.controller;

import com.fkis.wsrts.service.api.SensorModel;
import com.fkis.wsrts.service.api.SensorService;
import com.fkis.wsrts.web.api.Sensor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sensors")
@RequiredArgsConstructor
public class SensorController {

  private final ModelMapper modelMapper;
  private final SensorService sensorService;

  @PostMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Results are ok", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = Sensor.class))}),
      @ApiResponse(responseCode = "400", description = "Invalid request",
          content = @Content)})
  @Operation(summary = "Registers a sensor")
  public Sensor registerSensor(@Parameter @RequestBody @Valid Sensor sensor) {
    SensorModel mappedSensor = modelMapper.map(sensor, SensorModel.class);
    SensorModel registeredSensor = sensorService.create(mappedSensor);
    return convert(registeredSensor);
  }

  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Results are ok", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = List.class))})})
  @Operation(summary = "Returns all registered sensors")
  public List<Sensor> getAllSensors() {
    return sensorService.getAll()
        .stream()
        .map(this::convert)
        .toList();
  }

  private Sensor convert(SensorModel sensorModel) {
    return modelMapper.map(sensorModel, Sensor.class);
  }
}
