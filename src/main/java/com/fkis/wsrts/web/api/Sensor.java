package com.fkis.wsrts.web.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Sensor {

  @NotNull
  @Schema(description = "Id of the sensor")
  private String id;

  @Schema(description = "Country where the sensor is located in", nullable = true)
  private String country;

  @Schema(description = "City where the sensor is located at", nullable = true)
  private String city;
}
