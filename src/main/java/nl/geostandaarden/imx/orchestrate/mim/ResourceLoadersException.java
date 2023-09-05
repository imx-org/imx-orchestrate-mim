package nl.geostandaarden.imx.orchestrate.mim;

public class ResourceLoadersException extends RuntimeException {

  public ResourceLoadersException(String message) {
    super(message);
  }

  public ResourceLoadersException(String message, Throwable cause) {
    super(message, cause);
  }
}
