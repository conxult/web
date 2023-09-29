package de.conxult.web.type;

/**
 *
 * @author joerg
 */
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;

@Provider
public class ConxultConverterProvider
  implements ParamConverterProvider {

  @Inject
  OffsetDateTimeConverter offsetDateTimeConverter;

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> type, Type genericType, Annotation[] annotations) {
    if (OffsetDateTime.class.equals(type)) {
      return (ParamConverter<T>)offsetDateTimeConverter;
    }
    return null;
  }
}