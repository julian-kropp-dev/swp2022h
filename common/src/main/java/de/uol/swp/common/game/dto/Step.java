package de.uol.swp.common.game.dto;

import de.uol.swp.common.game.animation.Animation;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.robot.Robots;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Object holding the steps done by the GameLogic during the FactoryPhase.
 *
 * <p>The object is used to communicate the steps done by the GameLogic when doing the FactoryPhase
 */
public class Step implements Serializable {

  private static final long serialVersionUID = -8497604142086005098L;
  private final String gameId;
  private final List<Animation> steps = new ArrayList<>();
  private final EnumMap<Robots, RobotInformation> latestRobotInformation =
      new EnumMap<>(Robots.class);

  public Step(String gameId) {
    this.gameId = gameId;
  }

  public void addAnimation(Animation animation) {
    steps.add(animation);
  }

  public String getGameId() {
    return gameId;
  }

  public List<Animation> getSteps() {
    return steps;
  }

  public Map<Robots, RobotInformation> getLatestRobotInformation() {
    return latestRobotInformation;
  }

  public void addRobotInformation(Robots robot, RobotInformation information) {
    latestRobotInformation.put(robot, information);
  }

  /** Represents information about a robot. */
  public static class RobotInformation implements Serializable {

    private static final long serialVersionUID = -1985477900223771638L;
    private final int health;
    private final FloorField currentField;
    private final Direction currentDirection;
    FloorField respawn;
    private final int receivedDamage;
    private final int latestCheckpoint;

    // feel free to extend with more information

    /**
     * Constructs a RobotInformation object with the specified parameters.
     *
     * @param health the health of the robot
     * @param currentField the current field of the robot
     * @param currentDirection the current direction of the robot
     * @param receivedDamage the received damage of the robot
     * @param latestCheckpoint the latest checkpoint of the robot
     * @param respawn the respawn field of the robot
     */
    public RobotInformation(
        int health,
        FloorField currentField,
        Direction currentDirection,
        int receivedDamage,
        int latestCheckpoint,
        FloorField respawn) {
      this.health = health;
      this.currentField = currentField;
      this.currentDirection = currentDirection;
      this.receivedDamage = receivedDamage;
      this.latestCheckpoint = latestCheckpoint;
      this.respawn = respawn;
    }

    public int getHealth() {
      return health;
    }

    public FloorField getCurrentField() {
      return currentField;
    }

    public Direction getCurrentDirection() {
      return currentDirection;
    }

    public int getReceivedDamage() {
      return receivedDamage;
    }

    public int getLatestCheckpoint() {
      return latestCheckpoint;
    }

    public FloorField getRespawn() {
      return respawn;
    }
  }
}
