package de.conxult.web.entity;

import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.BaseEntity;
import de.conxult.pa.entity.IdEntity;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "roles", schema = WebConfiguration.SCHEMA)
@PaHistoryTable
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

    @PrePersist
    @Override
    public void onPrePersist() {
        id = UUID.nameUUIDFromBytes(name.getBytes());
        super.onPrePersist();
    }
}
