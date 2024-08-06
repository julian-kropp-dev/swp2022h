package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/** Message that the Gui-Animation is finished. */
public class GuiIsFinishMessage extends AbstractServerMessage {

  private static final long serialVersionUID = 3530415191489656868L;
  private final User user;

  private final String gameId;

  public GuiIsFinishMessage(User user, String gameId) {
    this.user = user;
    this.gameId = gameId;
  }

  /**
   * Get the user, who the gui-animation is finish.
   *
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * Get the gameId of the message.
   *
   * @return the gameId of the game
   */
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
    GuiIsFinishMessage that = (GuiIsFinishMessage) o;
    return user.equals(that.user) && gameId.equals(that.gameId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), user, gameId);
  }
}
