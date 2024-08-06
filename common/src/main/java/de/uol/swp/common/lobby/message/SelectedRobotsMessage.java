package de.uol.swp.common.lobby.message;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.user.UserDTO;
import java.util.List;
import java.util.Objects;

/** Message that will be sent to all members of the lobby when robots are updated. */
public class SelectedRobotsMessage extends AbstractLobbyMessage {

  private static final long serialVersionUID = 7571975064633572833L;

  private final List<Robots> robotsSelected;

  /**
   * Constructor for SelectedRobotsMessage.
   *
   * @param lobbyId Lobby
   * @param user User
   * @param robotsSelected Robots
   */
  public SelectedRobotsMessage(String lobbyId, UserDTO user, List<Robots> robotsSelected) {
    super(lobbyId, user);
    this.robotsSelected = robotsSelected;
  }

  public List<Robots> getRobotsSelected() {
    return robotsSelected;
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
    SelectedRobotsMessage that = (SelectedRobotsMessage) o;
    return Objects.equals(robotsSelected, that.robotsSelected);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), robotsSelected);
  }
}
