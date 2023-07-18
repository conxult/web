package de.conxult.web.entity;

import de.conxult.web.CxWebConstants;
import de.conxult.pa.entity.BaseEntity;
import de.conxult.pa.entity.IdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "roles", schema = CxWebConstants.SCHEMA)
@Getter @Setter @Accessors(chain = true)
public class Role
  extends    BaseEntity<Role>
  implements IdEntity<Role> {

  public final static String   ADMIN = "admin";
  public final static String   USER  = "user";
  public final static String   GUEST = "guest";

  public final static String[] ROLES = { ADMIN, USER, GUEST };

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @Column(name = "name")
  String name;

}
