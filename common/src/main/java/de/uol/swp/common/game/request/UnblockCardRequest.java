package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import java.util.Objects;

/** Request for unblocking the selection of programming cards. */
public class UnblockCardRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = -1687254357530110813L;
  private final int toUnblock;
  private final String gameId;
  private final String username;

  /**
   * Constructs a UnblockCardRequest.
   *
   * @param username the username associated with the request
   * @param toUnblock the slot requested to unblock
   * @param gameId the ID of the game where the player needs to unblock
   */
  public UnblockCardRequest(String username, String gameId, int toUnblock) {
    this.toUnblock = toUnblock;
    this.gameId = gameId;
    this.username = username;
  }

  /**
   * Retrieves the user associated with the interaction.
   *
   * @return the user associated with the interaction
   */
  public String getUsername() {
    return username;
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
  public int getToUnblock() {
    return toUnblock;
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
    UnblockCardRequest that = (UnblockCardRequest) o;
    return toUnblock == that.toUnblock
        && Objects.equals(gameId, that.gameId)
        && Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), toUnblock, gameId, username);
  }
}
