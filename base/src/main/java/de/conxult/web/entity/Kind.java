/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conxult.util.ToDo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 *
 * @author joerg
 */
@Embeddable
@Getter @Setter @Accessors(chain = true)
public class Kind {

    @ToDo("@Inject 'somehow'")
    static ObjectMapper objectMapper = new ObjectMapper();

    @Column(name = "kind_type")
    String type;

    @Column(name = "kind_json")
    String json;

    public static Kind of(Object value) {
        return new Kind().setValue(value);
    }

    public Class<?> getKindClass() {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException classNotFoundException) {
            return Void.class;
        }
    }

    public Kind setValue(Object value) {
        setType(value.getClass().getName());
        try {
            setJson(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException jsonProcessingException) {
            setJson("{}");
        }
        return this;
    }

    public Object getValue() {
        try {
            return objectMapper.readValue(json, getKindClass());
        } catch (JsonProcessingException jsonProcessingException) {
            return null;
        }
    }

    public <Type> Type getValue(Class<Type> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException jsonProcessingException) {
            return null;
        }
    }

}
