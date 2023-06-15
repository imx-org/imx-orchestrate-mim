# orchestrate-mim
MIM bindings for orchestrate

## Building the project

The project depends on the following projects
* https://github.com/dotwebstack/orchestrate
* https://github.com/dotwebstack/mim-java-sdk

which are not yet available in a public repository.
For now you should build these projects locally before building this project.

To build, run

```
mvn clean package
```

This will build the project and generate SPI configuration/metadata for plugin purposes.
