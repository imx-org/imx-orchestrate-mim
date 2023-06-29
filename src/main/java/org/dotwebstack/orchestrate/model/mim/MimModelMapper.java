package org.dotwebstack.orchestrate.model.mim;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.dotwebstack.orchestrate.model.mim.CardinalityHelper.getCardinalityOrDefault;
import static org.dotwebstack.orchestrate.model.mim.TypeHelper.getValueTypeName;
import static org.dotwebstack.orchestrate.model.mim.TypeHelper.isScalarLike;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
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
import org.dotwebstack.orchestrate.model.Attribute;
import org.dotwebstack.orchestrate.model.Cardinality;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.ObjectType;
import org.dotwebstack.orchestrate.model.ObjectTypeRef;
import org.dotwebstack.orchestrate.model.Property;
import org.dotwebstack.orchestrate.model.Relation;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

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
        .filter(not(Objecttype::isIndicatieAbstractObject))
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
    var objectTypeBuilder = ObjectType.builder();

    objectTypeBuilder.name(objecttype.getNaam())
        .properties(processProperties(objecttype));

    return objectTypeBuilder.build();
  }

  private ObjectType toObjectType(Gegevensgroeptype gegevensgroeptype) {
    var objectTypeBuilder = ObjectType.builder();

    objectTypeBuilder.name(gegevensgroeptype.getNaam())
        .properties(processProperties(gegevensgroeptype));

    return objectTypeBuilder.build();
  }

  private ObjectType toObjectType(GestructureerdDatatype gestructureerdDatatype) {
    var objectTypeBuilder = ObjectType.builder();

    objectTypeBuilder.name(gestructureerdDatatype.getNaam())
        .properties(processProperties(gestructureerdDatatype));

    return objectTypeBuilder.build();
  }

  private Set<Property> processProperties(Objecttype objecttype) {
    return Stream.of(objecttype.getAttribuutsoorten(true)
                .stream()
                .map(this::toProperty),
            objecttype.getRelatiesoorten(true)
                .stream()
                .map(this::toRelation),
            objecttype.getGegevensgroepen(true)
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
          .cardinality(getCardinalityOrDefault(attribuutsoort.getKardinaliteit(), Cardinality.OPTIONAL))
          .build();
    }

    return Relation.builder()
        .name(attribuutsoort.getNaam())
        .identifier(attribuutsoort.isIdentificerend())
        .target(toObjectTypeRef(attribuutsoort.getDatatype()))
        .cardinality(getCardinalityOrDefault(attribuutsoort.getKardinaliteit(), Cardinality.OPTIONAL))
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
          .cardinality(getCardinalityOrDefault(dataElement.getKardinaliteit(), Cardinality.OPTIONAL))
          .build();
    }

    return Relation.builder()
        .name(dataElement.getNaam())
        .identifier(dataElement.isIdentificerend())
        .target(toObjectTypeRef(dataElement.getDatatype()))
        .cardinality(getCardinalityOrDefault(dataElement.getKardinaliteit(), Cardinality.OPTIONAL))
        .build();
  }

  private Relation toRelation(Relatiesoort relatiesoort) {
    return Relation.builder()
        .name(relatiesoort.getNaam())
        .identifier(relatiesoort.isIdentificerend())
        .target(toObjectTypeRef(relatiesoort.getDoel()))
        .cardinality(getCardinalityOrDefault(relatiesoort.getKardinaliteit(), Cardinality.OPTIONAL))
        .inverseName(relatiesoort.getInverseNaam())
        .inverseCardinality(getCardinalityOrDefault(relatiesoort.getInverseKardinaliteit(), Cardinality.MULTI))
        .build();
  }

  private Relation toRelation(Gegevensgroep gegevensgroep) {
    return Relation.builder()
        .name(gegevensgroep.getNaam())
        .target(toObjectTypeRef(gegevensgroep.getType()))
        .cardinality(getCardinalityOrDefault(gegevensgroep.getKardinaliteit(), Cardinality.OPTIONAL))
        .build();
  }

  private ObjectTypeRef toObjectTypeRef(Modelelement modelelement) {
    return ObjectTypeRef.forType(modelelement.getNaam());
  }
}
