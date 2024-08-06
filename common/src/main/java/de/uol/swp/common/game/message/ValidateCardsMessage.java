package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/** Message that the Games starts. */
public class ValidateCardsMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -236890380362932300L;
  private final String gameId;

  public ValidateCardsMessage(String gameId) {
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
    ValidateCardsMessage that = (ValidateCardsMessage) o;
    return Objects.equals(gameId, that.gameId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), gameId);
  }
}
