package de.uol.swp.common.game.request;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;
import javax.annotation.Nonnull;

/** This request is sent from the player in a game, to request a new round. */
public class RespawnDirectionInteractionRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = -5187984110982039L;
  private final String gameId;
  private final User user;
  private final Direction direction;

  /**
   * Request to force a start of the next game round.
   *
   * @param gameId the gameId of the Game
   * @param user the user (owner) that force a new round.
   */
  public RespawnDirectionInteractionRequest(
      @Nonnull String gameId, @Nonnull User user, Direction direction) {
    this.gameId = gameId;
    this.user = user;
    this.direction = direction;
  }

  /**
   * Getter.
   *
   * @return the gameId of the game
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Getter.
   *
   * @return the user of the request.
   */
  public User getUser() {
    return user;
  }

  public Direction getDirection() {
    return direction;
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
    RespawnDirectionInteractionRequest that = (RespawnDirectionInteractionRequest) o;
    return gameId.equals(that.gameId) && user.equals(that.user) && direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), gameId, user, direction);
  }
}
