package de.uol.swp.common.game.player;

import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.user.User;
import java.io.Serializable;
import java.util.Objects;

/** This class represents the player. */
public class Player implements Serializable {

  private static final long serialVersionUID = -85813708519545974L;
  private final PlayerType playerType;
  private final User user;
  private final String uuid;
  private Robot robot;

  /**
   * The constructor of the class.
   *
   * @param playerType Describes the type of player
   * @param robot The robot assigned to the player
   * @param user the user
   */
  public Player(PlayerType playerType, Robot robot, User user) {
    this.playerType = playerType;
    this.robot = robot;
    this.user = user;
    this.uuid = user.getUUID();
  }

  public PlayerType getPlayerType() {
    return playerType;
  }

  public String getName() {
    return user.getUsername();
  }

  public Robot getRobot() {
    return robot;
  }

  public void setRobot(Robot robot) {
    this.robot = robot;
  }

  public User getUser() {
    return user;
  }

  public boolean isAiPlayer() {
    return playerType != PlayerType.HUMAN_PLAYER;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Player player = (Player) o;
    return Objects.equals(uuid, player.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }

  /**
   * Checks if user is spectator.
   *
   * @return if user is a spectator or not
   */
  public boolean isNotSpectator() {
    return robot.getLives() > 0;
  }
}
