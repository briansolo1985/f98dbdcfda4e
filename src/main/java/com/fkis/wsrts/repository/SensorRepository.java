package com.fkis.wsrts.repository;

import com.fkis.wsrts.repository.entity.SensorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorRepository extends JpaRepository<SensorEntity, String> {

}
