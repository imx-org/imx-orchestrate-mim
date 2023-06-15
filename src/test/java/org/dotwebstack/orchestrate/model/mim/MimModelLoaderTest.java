package org.dotwebstack.orchestrate.model.mim;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URISyntaxException;
import java.nio.file.Path;
import nl.geostandaarden.mim.parser.xml.ModelParser;
import org.junit.jupiter.api.Test;

class MimModelLoaderTest {

  @Test
  void test() throws URISyntaxException {
    var sourcePath = Path.of(ClassLoader.getSystemResource("mimxml-imx-geo.xml").toURI());

    var mimModel = ModelParser.parse(sourcePath);

    var model = MimModelMapper.getInstance().fromModel("imx-geo", mimModel);

    assertThat(model).isNotNull();
  }
}
