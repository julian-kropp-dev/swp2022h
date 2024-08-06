package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;

/** This request is sent from the player in a game, to request a new round. */
public class NextRoundStartForceRequest extends AbstractRequestMessage {
  private final String gameId;
  private final User user;

  /**
   * Request to force a start of the next game round.
   *
   * @param gameId the gameId of the Game
   * @param user the user (owner) that force a new round.
   */
  public NextRoundStartForceRequest(String gameId, User user) {
    this.gameId = gameId;
    this.user = user;
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
