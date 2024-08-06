package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/** Message that the Games starts. */
public class GameStartMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -85813708519545474L;
  private final String gameId;

  public GameStartMessage(String gameId) {
    this.gameId = gameId;
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
    GameStartMessage that = (GameStartMessage) o;
    return Objects.equals(gameId, that.gameId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId);
  }
}
