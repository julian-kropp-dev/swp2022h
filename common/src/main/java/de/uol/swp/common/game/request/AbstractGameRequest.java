package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import java.util.Objects;
import java.util.UUID;

/**
 * This class extends the AbstractRequestMessage class and provides additional functionality
 * specific to game requests.
 */
public class AbstractGameRequest extends AbstractRequestMessage {
  private final String username;
  private final String gameId;
  private final RequestType requestType;
  private final long timeStamp;
  private final String requestId;

  /**
   * Constructor.
   *
   * @param requestType The type of request
   */
  protected AbstractGameRequest(
      String username, String lobbyId, RequestType requestType, long timeStamp) {
    this.username = username;
    this.gameId = lobbyId;
    this.requestType = requestType;
    this.timeStamp = timeStamp;
    this.requestId = UUID.randomUUID().toString();
  }

  /**
   * The getTimeStamp function returns the timeStamp variable.
   *
   * @return The timeStamp variable
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getUsername() {
    return username;
  }

  public String getGameId() {
    return gameId;
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
    AbstractGameRequest that = (AbstractGameRequest) o;
    return Objects.equals(username, that.username)
        && Objects.equals(gameId, that.gameId)
        && requestType == that.requestType
        && Objects.equals(requestId, that.requestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), username, gameId, requestType, requestId);
  }
}
