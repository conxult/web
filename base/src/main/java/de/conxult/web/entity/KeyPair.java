package de.conxult.web.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.BaseEntity;
import de.conxult.pa.entity.IdEntity;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "key_pairs", schema = WebConfiguration.SCHEMA,
  indexes = {
      @Index(name = "idx1", unique = true, columnList = "scope, name" )})
@PaHistoryTable
@JsonInclude(Include.NON_NULL)
@Getter @Setter @Accessors(chain = true)
public class KeyPair
  extends    BaseEntity<KeyPair>
  implements IdEntity<KeyPair> {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  UUID id;

  @Column(name = "scope", nullable = false, updatable = false)
  String scope;

  @Column(name = "name", nullable = false, updatable = false)
  String name;

  @Column(name = "type", nullable = false, updatable = false)
  String type;

  @Column(name = "revoked_at")
  OffsetDateTime revokedAt;

  @Column(name = "revoked_by")
  UUID revokedBy;

  @Column(name = "public_key", nullable = false, updatable = false)
  String publicKey;

  @Column(name = "public_owner_id")
  UUID publicOwnerId;

  @Column(name = "private_key", nullable = false, updatable = false)
  String privateKey;

  @Column(name = "private_owner_id")
  UUID privateOwnerId;

}
