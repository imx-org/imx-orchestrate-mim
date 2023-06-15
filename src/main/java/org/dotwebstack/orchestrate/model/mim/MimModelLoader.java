package org.dotwebstack.orchestrate.model.mim;

import com.google.auto.service.AutoService;
import java.nio.file.Path;
import java.util.Optional;
import nl.geostandaarden.mim.parser.xml.ModelParser;
import org.dotwebstack.orchestrate.model.Model;
import org.dotwebstack.orchestrate.model.loader.ModelLoader;

@AutoService(ModelLoader.class)
public final class MimModelLoader implements ModelLoader {

  @Override
  public String getProfile() {
    return "MIM";
  }

  @Override
  public Optional<Model> loadModel(String alias, String location) {
    return ResourceLoaders.getResource(location)
        .map(resourcePath -> loadModel(alias, resourcePath));
  }

  private Model loadModel(String alias, Path path) {
    var mimModel = ModelParser.parse(path);

    return MimModelMapper.getInstance()
        .fromModel(alias, mimModel);
  }
}
