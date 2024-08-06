package de.uol.swp.server.message;

import java.util.Objects;

/**
 * This message is used if something went wrong
 *
 * <p>This ServerMessage is used if something went wrong e.g. in the login process
 *
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.usermanagement.AuthenticationService#onLoginRequest
 */
public class ServerExceptionMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 759153395742782002L;
  private final Exception exception;

  /**
   * Constructor.
   *
   * @param exception the Exception that is the reason for the creation of this
   */
  public ServerExceptionMessage(Exception exception) {
    super();
    this.exception = exception;
  }

  /**
   * Getter for the Exception.
   *
   * @return Exception passed in the constructor
   */
  public Exception getException() {
    return exception;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ServerExceptionMessage that = (ServerExceptionMessage) o;
    return Objects.equals(exception, that.exception);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), exception);
  }
}
