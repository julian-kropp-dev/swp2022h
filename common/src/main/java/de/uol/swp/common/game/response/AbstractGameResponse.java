package de.uol.swp.common.game.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/**
 * An abstract class representing a response message sent from the server to the client during a
 * game.
 */
public abstract class AbstractGameResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 5918584238374003666L;
  private final String requestId;
  private final String gameId;
  private final String userId;
  private final boolean success;

  protected AbstractGameResponse(String requestId, String gameId, String userId, boolean success) {
    this.requestId = requestId;
    this.gameId = gameId;
    this.userId = userId;
    this.success = success;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getGameId() {
    return gameId;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isSuccess() {
    return success;
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
    AbstractGameResponse that = (AbstractGameResponse) o;
    return success == that.success
        && Objects.equals(requestId, that.requestId)
        && Objects.equals(gameId, that.gameId)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), requestId, gameId, userId, success);
  }
}
