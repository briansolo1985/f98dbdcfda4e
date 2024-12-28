package com.fkis.wsrts.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "SENSOR")
@Data
public class SensorEntity {

  @Id
  @Column(name = "ID")
  private String id;

  @Column(name = "COUNTRY")
  private String country;

  @Column(name = "CITY")
  private String city;
}
