package de.conxult.web.entity;

import de.conxult.web.CxWebConstants;
import de.conxult.pa.entity.BaseEntity;
import de.conxult.pa.entity.IdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "pendings", schema = CxWebConstants.SCHEMA)
@Getter @Setter @Accessors(chain = true)
public class Pending
  extends    BaseEntity<Pending>
  implements IdEntity<Pending> {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @Column(name = "kind")
  String kind;

  @Column(name = "state")
  String state;

  @Column(name = "token")
  String token;

  @Column(name = "code")
  String code;

  @Column(name = "valid_until")
  OffsetDateTime validUntil;

  public Enum getState() {
    try {
      return (Enum)Class.forName(kind)
        .getMethod("valueOf", String.class)
        .invoke(null, state);
    } catch (Exception any) {
      return null;
    }
  }

  public Pending setState(Enum state) {
    this.kind = state.getDeclaringClass().getName();
    this.state = state.name();
    return this;
  }

}
