package com.fkis.wsrts.repository;

import com.fkis.wsrts.repository.entity.SensorMetricEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorMetricRepository extends JpaRepository<SensorMetricEntity, Long> {

  Optional<SensorMetricEntity> findFirstBySensorIdInOrderByTimestampDesc(@Param("sensorIds") List<String> sensorIds);

  Optional<SensorMetricEntity> findFirstByOrderByTimestampDesc();
}
