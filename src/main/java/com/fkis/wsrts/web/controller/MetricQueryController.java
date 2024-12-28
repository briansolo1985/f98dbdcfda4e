package com.fkis.wsrts.web.controller;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import com.fkis.wsrts.service.api.MetricField;
import com.fkis.wsrts.service.api.MetricQueryModel;
import com.fkis.wsrts.service.api.MetricQueryService;
import com.fkis.wsrts.web.api.MetricQueryResponse;
import com.fkis.wsrts.web.api.Sensor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/query")
@RequiredArgsConstructor
public class MetricQueryController {

  private final ModelMapper modelMapper;
  private final MetricQueryService metricQueryService;

  @GetMapping
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Results are ok", content = {
          @Content(mediaType = "application/json",
              schema = @Schema(implementation = Sensor.class))})})
  @Operation(summary = "Calculate average of senors' metrics for a given time interval")
  public MetricQueryResponse receiveMetric(
      @RequestParam(required = false) @Parameter List<String> sensorIds,
      @RequestParam(required = false) @Parameter List<String> fields,
      @RequestParam(required = false) @Parameter Duration range,
      @RequestParam(required = false) @Parameter Long rangeEnd) {

    List<String> ids = ofNullable(sensorIds).orElseGet(List::of);

    List<MetricField> metricFields = ofNullable(fields).orElseGet(List::of)
        .stream()
        .map(MetricField::forName)
        .flatMap(Optional::stream)
        .collect(collectingAndThen(
            toList(),
            result -> result.isEmpty() ? asList(MetricField.values()) : result
        ));

    MetricQueryModel metricQueryModel = range == null
        ? metricQueryService.fetchLatestMetric(ids, metricFields)
        : metricQueryService.calculateAverageMetrics(ids, metricFields, range, rangeEnd);
    return modelMapper.map(metricQueryModel, MetricQueryResponse.class);
  }
}
