package de.uol.swp.common.user.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** Class for UpdateUserExceptionMessage. */
public class UpdateUserExceptionMessage extends AbstractResponseMessage {

  private static final long serialVersionUID = -4014979035754820884L;
  private final String message;

  public UpdateUserExceptionMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "UpdateUserExceptionMessage " + message;
  }

  /**
   * Get the raw message from the Exception.
   *
   * @return message returned the raw message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateUserExceptionMessage that = (UpdateUserExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message);
  }
}
