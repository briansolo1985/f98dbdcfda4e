package com.fkis.wsrts.web.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class MetricQueryResponse {

  @Schema(description = "List of the queried sensors. If empty, all sensors were included")
  private List<String> sensorIds;
  @Schema(description = "From timestamp")
  private long fromTimestamp;
  @Schema(description = "To timestamp")
  private long toTimestamp;
  @Schema(description = "Query type, whether the translated query was a truly average calculation or a simple latest fetch")
  private String queryType;
  @Schema(description = "Metrics calculated for the given timestamp")
  private Map<String, Double> results;
}
