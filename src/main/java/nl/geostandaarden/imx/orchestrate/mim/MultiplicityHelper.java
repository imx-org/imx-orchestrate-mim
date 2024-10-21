package nl.geostandaarden.imx.orchestrate.mim;

import static nl.geostandaarden.imx.orchestrate.model.Multiplicity.MULTI;
import static nl.geostandaarden.imx.orchestrate.model.Multiplicity.OPTIONAL;
import static nl.geostandaarden.imx.orchestrate.model.Multiplicity.REQUIRED;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;
import nl.geostandaarden.mim.model.Kardinaliteit;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiplicityHelper {

  public static Multiplicity getMultiplicityOrDefault(Kardinaliteit kardinaliteit, Multiplicity defaultMultiplicity) {
    if (kardinaliteit == null) {
      return defaultMultiplicity;
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
