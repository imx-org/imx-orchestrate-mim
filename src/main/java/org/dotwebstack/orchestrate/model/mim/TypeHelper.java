package org.dotwebstack.orchestrate.model.mim;

import static java.util.function.Predicate.isEqual;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.CHARACTER_STRING;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DATE;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DATETIME;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.DECIMAL;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.REAL;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.URI;
import static org.dotwebstack.orchestrate.model.mim.MimDatatypes.YEAR;
import static org.dotwebstack.orchestrate.model.types.ScalarTypes.DOUBLE;
import static org.dotwebstack.orchestrate.model.types.ScalarTypes.FLOAT;
import static org.dotwebstack.orchestrate.model.types.ScalarTypes.STRING;

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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeHelper {

  public static String getValueTypeName(Attribuutsoort attribuutsoort) {
    var datatype = attribuutsoort.getDatatype();
    var datatypeKeuze = attribuutsoort.getDatatypekeuze();

    if (datatype == null && datatypeKeuze == null) {
      throw new IllegalArgumentException(
          String.format("No MIM Datatype or Datatype Keuze found for Attribuutsoort %s", attribuutsoort));
    }

    if (datatypeKeuze != null) {
      return getValueTypeName(datatypeKeuze);
    }

    return getValueTypeName(datatype);
  }

  public static String getValueTypeName(DataElement dataElement) {
    var datatype = dataElement.getDatatype();

    return getValueTypeName(datatype);
  }

  public static String getValueTypeName(Keuze keuze) {
    if (keuze.getDatatypen()
        .stream()
        .allMatch(dt -> dt.getNaam()
            .startsWith("GM_"))) {
      return "Geometry";
    }

    if (keuze.getDatatypen()
        .stream()
        .map(TypeHelper::resolveName)
        .distinct()
        .count() <= 1) {
      return keuze.getDatatypen()
          .stream()
          .findFirst()
          .map(TypeHelper::getValueTypeName)
          .orElseThrow();
    }

    throw new IllegalArgumentException(String.format("Unsupported MIM Keuze encountered: %s", keuze));
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

  public static String getValueTypeName(@NonNull Datatype datatype) {
    if (datatype instanceof Enumeratie || datatype instanceof Codelijst) {
      return STRING.getName();
    }

    if (datatype instanceof PrimitiefDatatype primitiefDatatype &&
        isNotBlank((primitiefDatatype).getFormeelPatroon())) {
      return STRING.getName();
    }

    if (datatype.getSupertypes(true).stream().map(Modelelement::getNaam)
        .anyMatch(isEqual(CHARACTER_STRING))) {
      return STRING.getName();
    }

    if (datatype.getNaam()
        .startsWith("GM_")) {
      return "Geometry";
    }

    var resolvedName = resolveName(datatype);

    return switch (resolvedName) {
      case CHARACTER_STRING, DATE, DATETIME, YEAR, URI -> STRING.getName();
      case REAL -> FLOAT.getName();
      case DECIMAL -> DOUBLE.getName();
      default -> resolvedName;
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
