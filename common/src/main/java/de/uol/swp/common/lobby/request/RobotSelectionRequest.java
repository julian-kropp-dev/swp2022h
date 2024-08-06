package de.uol.swp.common.lobby.request;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Request send when a Player select or unselect a Robot in a Lobby. */
public class RobotSelectionRequest extends AbstractLobbyRequest {
  private static final long serialVersionUID = 6796602698983656374L;
  private final Robots style;

  /**
   * Constructor.
   *
   * @param lobbyId Id of the Lobby
   * @param style which Robot will be claimed or unclaimed
   */
  public RobotSelectionRequest(String lobbyId, Robots style, UserDTO user) {
    super.lobbyId = lobbyId;
    this.style = style;
    super.user = user;
  }

  public Robots getStyle() {
    return style;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RobotSelectionRequest that = (RobotSelectionRequest) o;
    return Objects.equals(super.lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
