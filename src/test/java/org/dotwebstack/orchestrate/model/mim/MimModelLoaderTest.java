package org.dotwebstack.orchestrate.model.mim;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import nl.geostandaarden.mim.parser.xml.ModelParser;
import org.dotwebstack.orchestrate.ext.spatial.GeometryTypeFactory;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;
import org.junit.jupiter.api.Test;

class MimModelLoaderTest {

  @Test
  void test() throws URISyntaxException {
    var sourcePath = Path.of(ClassLoader.getSystemResource("mimxml-imx-geo.xml").toURI());
    var mimModel = ModelParser.parse(sourcePath);
    var valueTypeRegistry = new ValueTypeRegistry()
        .register(new GeometryTypeFactory());

    var model = new MimModelMapper(valueTypeRegistry)
        .fromModel(mimModel);

    assertThat(model).isNotNull();
  }
}
