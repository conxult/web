/*
 * Copyright by https://conxult.de
 */
package de.conxult.web.type;

import de.conxult.util.TimeUtil;
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
    return TimeUtil.toOffsetDateTime(text);
  }

  @Override
  public String toString(OffsetDateTime t) {
    return TimeUtil.toString(t);
  }

}
