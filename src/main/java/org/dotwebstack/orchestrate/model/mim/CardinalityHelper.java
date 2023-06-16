package org.dotwebstack.orchestrate.model.mim;

import static org.dotwebstack.orchestrate.model.Cardinality.MULTI;
import static org.dotwebstack.orchestrate.model.Cardinality.OPTIONAL;
import static org.dotwebstack.orchestrate.model.Cardinality.REQUIRED;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import nl.geostandaarden.mim.model.Kardinaliteit;
import org.dotwebstack.orchestrate.model.Cardinality;

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
