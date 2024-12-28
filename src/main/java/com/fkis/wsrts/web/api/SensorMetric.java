package com.fkis.wsrts.web.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorMetric {

  @Schema(description = "Temperature in Celsius degrees", nullable = true)
  private Double temperature;

  @Schema(description = "Air humidity", nullable = true)
  private Double humidity;

  @Schema(description = "Wind speed in kilometer per hours", nullable = true)
  private Double windSpeed;

  @Schema(description = "Air pressure in millibars", nullable = true)
  private Double airPressure;

  @NotNull
  @Schema(description = "Timestamp in UNIX epoch format, milliseconds precision")
  private long timestamp;
}
