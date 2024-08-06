package de.uol.swp.common.lobby.response;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Response to SelectRobotRequest. */
public class RobotSelectedResponse extends AbstractResponseMessage {
  private static final long serialVersionUID = -7880826709735317954L;
  String lobbyId;
  Boolean alreadyClaimed;
  Robots style;
  Boolean selected;
  UserDTO user;

  /**
   * Constructor.
   *
   * @param lobbyId in which lobby the robot is now selected or unselected
   * @param selected was the robot already selected
   */
  public RobotSelectedResponse(
      String lobbyId, Boolean alreadyClaimed, Robots style, Boolean selected, UserDTO user) {
    this.lobbyId = lobbyId;
    this.alreadyClaimed = alreadyClaimed;
    this.style = style;
    this.selected = selected;
    this.user = user;
  }

  /**
   * Getter for the lobbyId.
   *
   * @return lobbyId
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for Boolean alreadySelected.
   *
   * @return where the Robot already claimed or not
   */
  public Boolean getAlreadyClaimed() {
    return alreadyClaimed;
  }

  /**
   * Getter for the Robot that was claimed.
   *
   * @return the claimed robot
   */
  public Robots getStyle() {
    return style;
  }

  /**
   * Getter for claimstatus.
   *
   * @return true if select was succesful/ false if not.
   */
  public Boolean getSelected() {
    return selected;
  }

  /**
   * Getter for the User who request a robot claim.
   *
   * @return User who request a robot claim.
   */
  public User getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RobotSelectedResponse that = (RobotSelectedResponse) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
