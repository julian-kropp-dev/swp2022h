package de.uol.swp.server.message;

import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.lobby.Lobby;
import java.util.Objects;

/**
 * The GameLogicFinish class represents a server internal message indicating that the game logic has
 * finished.
 *
 * <p>processing a certain step in a given lobby.
 */
public class GameLogicFinish extends AbstractServerInternalMessage {

  private static final long serialVersionUID = -519787981582039L;
  private final Step step;
  private final Lobby lobby;

  public GameLogicFinish(Step step, Lobby lobby) {
    this.step = step;
    this.lobby = lobby;
  }

  /**
   * Returns the Lobby object representing the lobby in which the step has been processed.
   *
   * @return The Lobby object representing the lobby in which the step has been processed
   */
  public Lobby getLobby() {
    return lobby;
  }

  /**
   * Returns the Step object representing the step that has been processed by the game logic.
   *
   * @return The Step object representing the step that has been processed by the game logic
   */
  public Step getStep() {
    return step;
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
    GameLogicFinish that = (GameLogicFinish) o;
    return Objects.equals(step, that.step) && Objects.equals(lobby, that.lobby);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), step, lobby);
  }
}
