package nl.geostandaarden.imx.orchestrate.mim;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static nl.geostandaarden.imx.orchestrate.mim.MultiplicityHelper.getMultiplicityOrDefault;
import static nl.geostandaarden.imx.orchestrate.mim.TypeHelper.isScalarLike;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nl.geostandaarden.imx.orchestrate.model.Attribute;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.Multiplicity;
import nl.geostandaarden.imx.orchestrate.model.ObjectType;
import nl.geostandaarden.imx.orchestrate.model.ObjectTypeRef;
import nl.geostandaarden.imx.orchestrate.model.Property;
import nl.geostandaarden.imx.orchestrate.model.Relation;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.mim.model.Attribuutsoort;
import nl.geostandaarden.mim.model.DataElement;
import nl.geostandaarden.mim.model.Gegevensgroep;
import nl.geostandaarden.mim.model.Gegevensgroeptype;
import nl.geostandaarden.mim.model.GestructureerdDatatype;
import nl.geostandaarden.mim.model.Informatiemodel;
import nl.geostandaarden.mim.model.Modelelement;
import nl.geostandaarden.mim.model.Objecttype;
import nl.geostandaarden.mim.model.Package;
import nl.geostandaarden.mim.model.Relatiesoort;

@RequiredArgsConstructor
public class MimModelMapper {

  private final ValueTypeRegistry valueTypeRegistry;

  public Model fromModel(Informatiemodel informatiemodel) {
    var modelBuilder = Model.builder();

    informatiemodel.getPackages()
        .forEach(mimPackage -> processPackage(mimPackage, modelBuilder));

    return modelBuilder.build();
  }

  private void processPackage(Package mimPackage, Model.ModelBuilder modelBuilder) {
    mimPackage.getObjecttypen()
        .stream()
        .map(this::toObjectType)
        .forEach(modelBuilder::objectType);

    mimPackage.getGegevensgroeptypen()
        .stream()
        .map(this::toObjectType)
        .forEach(modelBuilder::objectType);

    mimPackage.getDatatypen()
        .stream()
        .filter(GestructureerdDatatype.class::isInstance)
        .map(GestructureerdDatatype.class::cast)
        .map(this::toObjectType)
        .forEach(modelBuilder::objectType);
  }

  private ObjectType toObjectType(Objecttype objecttype) {
    return ObjectType.builder()
        .name(objecttype.getNaam())
        .supertypes(objecttype.getSupertypes()
                .stream()
                .map(Objecttype::getNaam)
                .toList())
        .properties(processProperties(objecttype))
        .build();
  }

  private ObjectType toObjectType(Gegevensgroeptype gegevensgroeptype) {
    return ObjectType.builder()
        .name(gegevensgroeptype.getNaam())
        .properties(processProperties(gegevensgroeptype))
        .build();
  }

  private ObjectType toObjectType(GestructureerdDatatype gestructureerdDatatype) {
    return ObjectType.builder()
        .name(gestructureerdDatatype.getNaam())
        .properties(processProperties(gestructureerdDatatype))
        .build();
  }

  private Set<Property> processProperties(Objecttype objecttype) {
    return Stream.of(objecttype.getAttribuutsoorten()
                .stream()
                .map(this::toProperty),
            objecttype.getRelatiesoorten()
                .stream()
                .filter(relatiesoort -> relatiesoort.getDoel() != null)
                .map(this::toRelation),
            objecttype.getGegevensgroepen()
                .stream()
                .map(this::toRelation))
        .reduce(Stream::concat)
        .orElseGet(Stream::empty)
        .collect(toUnmodifiableSet());
  }

  private Set<Property> processProperties(Gegevensgroeptype gegevensgroeptype) {
    return Stream.of(gegevensgroeptype.getAttribuutsoorten()
                .stream()
                .map(this::toProperty),
            gegevensgroeptype.getRelatiesoorten()
                .stream()
                .map(this::toRelation),
            gegevensgroeptype.getGegevensgroepen()
                .stream()
                .map(this::toRelation))
        .reduce(Stream::concat)
        .orElseGet(Stream::empty)
        .collect(toUnmodifiableSet());
  }

  private Set<Property> processProperties(GestructureerdDatatype gestructureerdDatatype) {
    return gestructureerdDatatype.getDataElementen()
        .stream()
        .map(this::toProperty)
        .collect(toUnmodifiableSet());
  }

  private Property toProperty(Attribuutsoort attribuutsoort) {
    if (isScalarLike(attribuutsoort)) {
      var valueTypeName = TypeHelper.getValueTypeName(attribuutsoort);
      var valueType = valueTypeRegistry.getValueTypeFactory(valueTypeName)
          .create(valueTypeName.equals("Geometry") ? Map.of("srid", 28992) : Map.of());

      return Attribute.builder()
          .name(attribuutsoort.getNaam())
          .identifier(attribuutsoort.isIdentificerend())
          .type(valueType)
          .multiplicity(getMultiplicityOrDefault(attribuutsoort.getKardinaliteit(), Multiplicity.OPTIONAL))
          .build();
    }

    return Relation.builder()
        .name(attribuutsoort.getNaam())
        .identifier(attribuutsoort.isIdentificerend())
        .target(toObjectTypeRef(attribuutsoort.getDatatype()))
        .multiplicity(getMultiplicityOrDefault(attribuutsoort.getKardinaliteit(), Multiplicity.OPTIONAL))
        .build();
  }

  private Property toProperty(DataElement dataElement) {
    if (isScalarLike(dataElement)) {
      var valueTypeName = TypeHelper.getValueTypeName(dataElement);
      var valueType = valueTypeRegistry.getValueTypeFactory(valueTypeName)
          .create(valueTypeName.equals("Geometry") ? Map.of("srid", 28992) : Map.of());

      return Attribute.builder()
          .name(dataElement.getNaam())
          .identifier(dataElement.isIdentificerend())
          .type(valueType)
          .multiplicity(getMultiplicityOrDefault(dataElement.getKardinaliteit(), Multiplicity.OPTIONAL))
          .build();
    }

    return Relation.builder()
        .name(dataElement.getNaam())
        .identifier(dataElement.isIdentificerend())
        .target(toObjectTypeRef(dataElement.getDatatype()))
        .multiplicity(getMultiplicityOrDefault(dataElement.getKardinaliteit(), Multiplicity.OPTIONAL))
        .build();
  }

  private Relation toRelation(Relatiesoort relatiesoort) {
    return Relation.builder()
        .name(relatiesoort.getNaam())
        .identifier(relatiesoort.isIdentificerend())
        .target(toObjectTypeRef(relatiesoort.getDoel()))
        .multiplicity(getMultiplicityOrDefault(relatiesoort.getKardinaliteit(), Multiplicity.OPTIONAL))
        .inverseName(relatiesoort.getInverseNaam())
        .inverseMultiplicity(getMultiplicityOrDefault(relatiesoort.getInverseKardinaliteit(), Multiplicity.MULTI))
        .build();
  }

  private Relation toRelation(Gegevensgroep gegevensgroep) {
    return Relation.builder()
        .name(gegevensgroep.getNaam())
        .target(toObjectTypeRef(gegevensgroep.getType()))
        .multiplicity(getMultiplicityOrDefault(gegevensgroep.getKardinaliteit(), Multiplicity.OPTIONAL))
        .build();
  }

  private ObjectTypeRef toObjectTypeRef(Modelelement modelelement) {
    return ObjectTypeRef.forType(modelelement.getNaam());
  }
}
