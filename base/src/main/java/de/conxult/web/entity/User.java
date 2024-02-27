package de.conxult.web.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.conxult.pa.annotation.PaHistoryTable;
import de.conxult.pa.entity.IdEntity;
import de.conxult.pa.entity.VersionEntity;
import de.conxult.web.WebConfiguration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Entity
@Table(name = "users", schema = WebConfiguration.SCHEMA)
@JsonInclude(Include.NON_NULL)
@PaHistoryTable
@Getter @Setter @Accessors(chain = true)
public class User
    extends    VersionEntity<User>
    implements IdEntity<User> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    UUID id;

    @Column(name = "nick_name")
    String nickName;

    @Column(name = "sur_name")
    String surName;

    @Column(name = "family_name")
    String familyName;

    @Column(name = "email")
    String email;

    @Column(name = "state")
    String state;

    @Transient
    Collection<String> roles = new TreeSet<>();

    public static enum State {

        ACTIVE,
        DISABLED,
        DELETED,
    }
}
