/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.entity;

import de.conxult.web.CxWebConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "quarkus", schema = CxWebConstants.SCHEMA)
public class Some {

  @Column(name = "id")
  @Id
  UUID id;

  @Column(name = "name")
  String name;

  public UUID getId() {
    return id;
  }

  public Some setId(UUID id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Some setName(String name) {
    this.name = name;
    return this;
  }





}
