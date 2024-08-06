package de.uol.swp.common.user.exception;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * UpdateUserStatsExceptionMessage is a response message indicating an exception occurred while
 * updating user statistics.
 */
public class UpdateUserStatsExceptionMessage extends AbstractResponseMessage {
  private static final long serialVersionUID = -401494567820884L;
  private final String message;

  public UpdateUserStatsExceptionMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "UpdateUserStatsExceptionMessage{" + "message='" + message + '\'' + '}';
  }

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
    if (!super.equals(o)) {
      return false;
    }
    UpdateUserStatsExceptionMessage that = (UpdateUserStatsExceptionMessage) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), message);
  }
}
