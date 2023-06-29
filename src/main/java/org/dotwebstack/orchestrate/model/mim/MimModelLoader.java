package org.dotwebstack.orchestrate.model.mim;

import com.google.auto.service.AutoService;
import java.nio.file.Path;
import nl.geostandaarden.mim.parser.xml.ModelParser;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;
import org.dotwebstack.orchestrate.model.types.ValueTypeRegistry;

@AutoService(ModelLoader.class)
public final class MimModelLoader implements ModelLoader {

  @Override
  public String getName() {
    return "mim";
  }

  @Override
  public Model load(String location, ValueTypeRegistry valueTypeRegistry) {
    var resourcePath = ResourceLoaders.getResource(location)
        .orElseThrow(() -> new RuntimeException("Could not load MIM model."));

    return loadModel(resourcePath, valueTypeRegistry);
  }

  private Model loadModel(Path path, ValueTypeRegistry valueTypeRegistry) {
    var mimModel = ModelParser.parse(path);

    return new MimModelMapper(valueTypeRegistry)
        .fromModel(mimModel);
  }
}
