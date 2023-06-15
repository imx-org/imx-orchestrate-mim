package org.dotwebstack.orchestrate.model.mim;

public class ResourceLoadersException extends RuntimeException {

  public ResourceLoadersException(String message) {
    super(message);
  }

  public ResourceLoadersException(String message, Throwable cause) {
    super(message, cause);
  }
}
