/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.type;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 *
 * @author joerg
 */
@ApplicationScoped
public class OffsetDateTimeDeserializer
  extends JsonDeserializer<OffsetDateTime> {

  @Inject
  OffsetDateTimeConverter converter;

  @Override
  public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context)
    throws IOException, JsonProcessingException {
    return converter.fromString(parser.getText());
  }
}
