/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.type;

import de.conxult.util.OffsetDateTimeUtil;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.ParamConverter;
import java.time.OffsetDateTime;

/**
 *
 * @author joerg
 */
@ApplicationScoped
@Unremovable
public class OffsetDateTimeConverter
  implements ParamConverter<OffsetDateTime> {

  @Override
  public OffsetDateTime fromString(String text) {
    return OffsetDateTimeUtil.of(text);
  }

  @Override
  public String toString(OffsetDateTime t) {
    return OffsetDateTimeUtil.toString(t);
  }

}
