package de.uol.swp.server.message;

import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.lobby.Lobby;
import java.util.Objects;

/**
 * The MoveRobotInternalMessage class represents a server internal message indicating that the robot
 * move command is finish.
 */
public class MoveRobotInternalMessage extends AbstractServerInternalMessage {
  private static final long serialVersionUID = -519787981582088L;
  private final Step step;
  private final Lobby lobby;

  public MoveRobotInternalMessage(Step step, Lobby lobby) {
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
    MoveRobotInternalMessage that = (MoveRobotInternalMessage) o;
    return Objects.equals(lobby.getLobbyId(), that.lobby.getLobbyId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), step, lobby);
  }
}
