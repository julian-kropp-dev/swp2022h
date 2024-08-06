package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.Step.RobotInformation;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;
import java.util.UUID;

/** This class is used to update the robot position and information in the gui. */
public class RobotInformationMessage extends AbstractServerMessage {

  private static final long serialVersionUID = 6059747329899356952L;

  private final RobotInformation robotInformation;
  private final Robots robotType;
  private final String uuid;

  private final String gameId;

  /**
   * Constructs a RobotInformationMessage with the specified parameters.
   *
   * @param robotType the type of the robot
   * @param robotInformation the information of the robot
   * @param gameId the ID of the game
   */
  public RobotInformationMessage(
      Robots robotType, RobotInformation robotInformation, String gameId) {
    this.robotType = robotType;
    this.robotInformation = robotInformation;
    this.uuid = UUID.randomUUID().toString();
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  public RobotInformation getRobotInformation() {
    return robotInformation;
  }

  public Robots getRobotType() {
    return robotType;
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
    RobotInformationMessage that = (RobotInformationMessage) o;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), uuid);
  }
}
