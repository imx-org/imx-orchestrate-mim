package nl.geostandaarden.imx.orchestrate.mim;

import static nl.geostandaarden.imx.orchestrate.model.Cardinality.MULTI;
import static nl.geostandaarden.imx.orchestrate.model.Cardinality.OPTIONAL;
import static nl.geostandaarden.imx.orchestrate.model.Cardinality.REQUIRED;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Cardinality;
import nl.geostandaarden.mim.model.Kardinaliteit;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CardinalityHelper {

  public static Cardinality getCardinalityOrDefault(Kardinaliteit kardinaliteit, Cardinality defaultCardinality) {
    if (kardinaliteit == null) {
      return defaultCardinality;
    }

    if (kardinaliteit.isMulti()) {
      return MULTI;
    }

    if (kardinaliteit.getMin() == 0) {
      return OPTIONAL;
    }

    return REQUIRED;
  }
}
