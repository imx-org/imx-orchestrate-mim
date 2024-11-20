package nl.geostandaarden.imx.orchestrate.mim;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceLoaders {

    private static final Path LOCAL_RESOURCE_PATH = Paths.get("/").toAbsolutePath();

    @SneakyThrows
    public static Optional<Path> getResource(@NonNull String resourceLocation) {

        var relativeResourcePath = resolve(LOCAL_RESOURCE_PATH, resourceLocation);
        if (Files.exists(relativeResourcePath)) {
            return Optional.of(relativeResourcePath);
        }

        var resourcePath = Paths.get(resourceLocation);
        if (Files.exists(resourcePath)) {
            return Optional.of(resourcePath);
        }

        URI classPathUri;
        try {
            classPathUri = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(resourceLocation)
                    .toURI();
        } catch (URISyntaxException uriSyntaxException) {
            throw new ResourceLoadersException(
                    String.format("Could not get URI for %s", resourceLocation), uriSyntaxException);
        }

        var systemResourcePath = Path.of(classPathUri);
        if (Files.exists(systemResourcePath)) {
            return Optional.of(systemResourcePath);
        }

        return Optional.empty();
    }

    private static Path resolve(@NonNull Path basePath, String resourceLocation) {
        if (resourceLocation == null) {
            return basePath;
        }
        return basePath.resolve(resourceLocation);
    }
}
