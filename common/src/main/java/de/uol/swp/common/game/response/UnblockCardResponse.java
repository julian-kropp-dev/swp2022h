package de.uol.swp.common.game.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Arrays;
import java.util.Objects;

/**
 * UnblockCardResponse is a response message indicating the success or failure of an unblock card
 * request.
 */
public class UnblockCardResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -1687254357530117305L;
  private final boolean[] blocked;
  private final String gameId;
  private final User user;

  /**
   * Constructs a UnblockCardResponse.
   *
   * @param user the user associated with the interaction
   * @param blocked the currently blocked card slots
   * @param gameId the ID of the game where the player needs to unblock
   */
  public UnblockCardResponse(User user, String gameId, boolean[] blocked) {
    this.blocked = blocked;
    this.gameId = gameId;
    this.user = user;
  }

  /**
   * Retrieves the user associated with the interaction.
   *
   * @return the user associated with the interaction
   */
  public User getUser() {
    return user;
  }

  /**
   * Retrieves the gameId.
   *
   * @return the gameId associated with the interaction
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Retrieves the currently blocked card slots.
   *
   * @return the slots involved in the interaction
   */
  public boolean[] getBlocked() {
    return blocked;
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
    UnblockCardResponse that = (UnblockCardResponse) o;
    return Arrays.equals(blocked, that.blocked)
        && Objects.equals(gameId, that.gameId)
        && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(super.hashCode(), gameId, user);
    result = 31 * result + Arrays.hashCode(blocked);
    return result;
  }
}
