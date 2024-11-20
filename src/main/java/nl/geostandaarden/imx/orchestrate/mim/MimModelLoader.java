package nl.geostandaarden.imx.orchestrate.mim;

import com.google.auto.service.AutoService;
import java.nio.file.Path;
import nl.geostandaarden.imx.orchestrate.model.Model;
import nl.geostandaarden.imx.orchestrate.model.loader.ModelLoader;
import nl.geostandaarden.imx.orchestrate.model.types.ValueTypeRegistry;
import nl.geostandaarden.mim.parser.xml.ModelParser;

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

        return new MimModelMapper(valueTypeRegistry).fromModel(mimModel);
    }
}
