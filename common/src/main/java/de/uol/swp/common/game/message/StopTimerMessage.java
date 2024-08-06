package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/** Message that the card selection timer has been stopped. */
public class StopTimerMessage extends AbstractServerMessage {
  private final String username;
  private final String gameId;

  public StopTimerMessage(String username, String gameId) {
    this.username = username;
    this.gameId = gameId;
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
    StopTimerMessage that = (StopTimerMessage) o;
    return Objects.equals(username, that.username) && Objects.equals(gameId, that.gameId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), username, gameId);
  }
}
