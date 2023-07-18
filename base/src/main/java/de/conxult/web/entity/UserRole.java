package de.conxult.web.entity;

import de.conxult.pa.entity.BaseEntity;
import de.conxult.web.CxWebConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "user_role", schema = CxWebConstants.SCHEMA)
@IdClass(UserRole.ID.class)
@Getter @Setter @Accessors(chain = true)
public class UserRole
  extends BaseEntity<UserRole> {

  @Id
  @Column(name = "user_id")
  UUID userId;

  @Id
  @Column(name = "role_id")
  UUID roleId;

  @Getter @Setter @Accessors(chain = true) @EqualsAndHashCode
  public static class ID {
      UUID userId;
      UUID roleId;
  }

}
