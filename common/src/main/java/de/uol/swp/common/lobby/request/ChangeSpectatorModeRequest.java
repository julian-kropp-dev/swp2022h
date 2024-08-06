package de.uol.swp.common.lobby.request;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Request for getting Information about and for the owner of the lobby. */
public class ChangeSpectatorModeRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = 2160747403718542271L;
  private final Robots robot;
  private final boolean robotSelected;

  /** Constructor. */
  public ChangeSpectatorModeRequest(
      String lobbyId, UserDTO user, Robots robot, boolean robotSelected) {
    super(lobbyId, user);
    this.robot = robot;
    this.robotSelected = robotSelected;
  }

  /**
   * Getter for robot.
   *
   * @return the selected robot
   */
  public Robots getRobot() {
    return robot;
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
    ChangeSpectatorModeRequest that = (ChangeSpectatorModeRequest) o;
    return robotSelected == that.robotSelected && robot == that.robot;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), robot, robotSelected);
  }
}
