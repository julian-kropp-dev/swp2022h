package de.uol.swp.server.message;

import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * A ServerInternalMessage used to Signal the GameService that a user leaving should be replaced
 * with an Ai if the Game exists.
 */
public class ReplaceUserWithAiMessage extends AbstractServerInternalMessage {

  private final User user;
  private final String lobbyId;

  public ReplaceUserWithAiMessage(User user, String lobbyId) {
    this.user = user;
    this.lobbyId = lobbyId;
  }

  /**
   * Getter for the User.
   *
   * @return the User who should be replaced with an Ai
   */
  public User getUser() {
    return user;
  }

  /**
   * Getter for the lobbyId.
   *
   * @return the lobbyId of the Lobby from which the user comes who should be replaced with an Ai
   */
  public String getLobbyId() {
    return lobbyId;
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
    ReplaceUserWithAiMessage that = (ReplaceUserWithAiMessage) o;
    return Objects.equals(user, that.user) && Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), user, lobbyId);
  }
}
