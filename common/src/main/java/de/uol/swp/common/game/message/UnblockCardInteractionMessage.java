package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.user.User;
import java.util.Arrays;
import java.util.Objects;

/** Message that the Server needs a Slot to unblock from the User. */
public class UnblockCardInteractionMessage extends AbstractServerMessage {
  private static final long serialVersionUID = -5187984569752039L;
  private final User user;
  private final String gameId;
  private final boolean[] blocked;
  private final int amount;

  /**
   * Constructs a UnblockCardInteractionMessage.
   *
   * @param user the user associated with the interaction
   * @param blocked the currently blocked card slots
   * @param gameId the ID of the game where the player needs to unblock
   * @param amount amount of cards to unblock
   */
  public UnblockCardInteractionMessage(User user, String gameId, boolean[] blocked, int amount) {
    this.user = user;
    this.blocked = blocked;
    this.gameId = gameId;
    this.amount = amount;
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

  /**
   * Retrieves the amount of the interaction.
   *
   * @return the amount of the interaction
   */
  public int getAmount() {
    return amount;
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
    UnblockCardInteractionMessage that = (UnblockCardInteractionMessage) o;
    return amount == that.amount
        && Objects.equals(user, that.user)
        && Objects.equals(gameId, that.gameId)
        && Arrays.equals(blocked, that.blocked);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(super.hashCode(), user, gameId, amount);
    result = 31 * result + Arrays.hashCode(blocked);
    return result;
  }
}
