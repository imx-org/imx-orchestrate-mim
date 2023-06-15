package org.dotwebstack.orchestrate.model.mim;

import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.BOOLEAN;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.CHARACTER_STRING;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DATE;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DATETIME;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DECIMAL;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.INTEGER;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.REAL;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.URI;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.YEAR;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.geostandaarden.mim.model.Attribuutsoort;
import nl.geostandaarden.mim.model.Codelijst;
import nl.geostandaarden.mim.model.DataElement;
import nl.geostandaarden.mim.model.Datatype;
import nl.geostandaarden.mim.model.Enumeratie;
import nl.geostandaarden.mim.model.GestructureerdDatatype;
import nl.geostandaarden.mim.model.Keuze;
import nl.geostandaarden.mim.model.Modelelement;
import nl.geostandaarden.mim.model.PrimitiefDatatype;
import nl.geostandaarden.mim.model.Referentielijst;
import org.dotwebstack.orchestrate.ext.spatial.GeometryType;
import org.dotwebstack.orchestrate.model.AttributeType;
import org.dotwebstack.orchestrate.model.types.ScalarTypes;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeHelper {

  public static AttributeType getValueType(Attribuutsoort attribuutsoort) {
    var datatype = attribuutsoort.getDatatype();
    var datatypeKeuze = attribuutsoort.getDatatypekeuze();

    if (datatype == null && datatypeKeuze == null) {
      throw new IllegalArgumentException(
          String.format("No MIM Datatype or Datatype Keuze found for Attribuutsoort %s", attribuutsoort));
    }

    if (datatypeKeuze != null) {
      return getValueType(datatypeKeuze);
    }

    return getValueType(datatype);
  }

  public static AttributeType getValueType(DataElement dataElement) {
    var datatype = dataElement.getDatatype();

    return getValueType(datatype);
  }

  public static AttributeType getValueType(Keuze keuze) {
    if (keuze.getDatatypen()
        .stream()
        .allMatch(dt -> dt.getNaam()
            .startsWith("GM_"))) {
      return new GeometryType();
    } else if (keuze.getDatatypen()
        .stream()
        .map(TypeHelper::resolveName)
        .distinct()
        .count() <= 1) {
      return keuze.getDatatypen()
          .stream()
          .findFirst()
          .map(TypeHelper::getValueType)
          .orElseThrow();
    } else {
      throw new IllegalArgumentException(String.format("Unsupported MIM Keuze encountered :%s", keuze));
    }
  }

  private static String resolveName(Datatype datatype) {
    var superTypes = datatype.getSupertypes(true);

    if (superTypes.isEmpty()) {
      return datatype.getNaam();
    }

    return superTypes.stream()
        .filter(superDatatype -> superDatatype.getSupertypes().isEmpty())
        .findFirst()
        .map(Modelelement::getNaam)
        .orElseThrow();
  }

  public static AttributeType getValueType(@NonNull Datatype datatype) {

    if (datatype instanceof Enumeratie || datatype instanceof Codelijst) {
      return ScalarTypes.STRING;
    }

    if (datatype instanceof PrimitiefDatatype primitiefDatatype &&
        isNotBlank((primitiefDatatype).getFormeelPatroon())) {
      return ScalarTypes.STRING;
    }

    if (datatype.getSupertypes(true).stream().map(Modelelement::getNaam)
        .anyMatch(isEqual(CHARACTER_STRING))) {
      return ScalarTypes.STRING;
    }

    if (datatype.getNaam()
        .startsWith("GM_")) {
      return new GeometryType();
    }

    var resolvedName = resolveName(datatype);

    return switch (resolvedName) {
      case CHARACTER_STRING, DATE, DATETIME, YEAR, URI -> ScalarTypes.STRING;
      case INTEGER -> ScalarTypes.INTEGER;
      case BOOLEAN -> ScalarTypes.BOOLEAN;
      case REAL -> ScalarTypes.FLOAT;
      case DECIMAL -> ScalarTypes.DOUBLE;
      default -> throw new IllegalArgumentException(
          String.format("No scalar type mapping for datatype '%s'", datatype));
    };
  }

  public static boolean isScalarLike(@NonNull Attribuutsoort attribuutsoort) {
    return isScalarLike(attribuutsoort.getDatatype());
  }

  public static boolean isScalarLike(@NonNull DataElement dataElement) {
    return isScalarLike(dataElement.getDatatype());
  }

  public static boolean isScalarLike(Datatype datatype) {
    return !(datatype instanceof GestructureerdDatatype || datatype instanceof Referentielijst);
  }
}
