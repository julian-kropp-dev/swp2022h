package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;

/** This request is sent from the player in a game to request a new round. */
public class NextRoundStartRequest extends AbstractRequestMessage {

  private final String gameId;
  private final User user;

  /**
   * Request to set a player to ready for the next game round.
   *
   * @param gameId the gameId of the Game
   * @param user the user to set ready
   */
  public NextRoundStartRequest(String gameId, User user) {
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
