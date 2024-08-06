package de.uol.swp.server.game.ai;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.request.RespawnDirectionInteractionRequest;
import de.uol.swp.common.game.request.RespawnInteractionRequest;
import de.uol.swp.common.game.robot.Robot;
import java.util.Random;

/**
 * This class represents a random Ai player that calculates the respawn. The class extends the base
 * class 'Ai'. It overrides the 'run()' method to provide its own implementation.
 */
@SuppressWarnings("UnstableApiUsage")
public class RespawnAi extends Ai {
  Integer[][] floorFields;
  boolean onlyDirection;

  public RespawnAi(Integer[][] floorFields) {
    this.floorFields = floorFields;
    this.onlyDirection = false;
  }

  public RespawnAi() {
    this.floorFields = null;
    this.onlyDirection = true;
  }

  /**
   * When an object implementing interface {@code Runnable} is used to create a thread, starting the
   * thread causes the object's {@code run} method to be called in that separately executing thread.
   *
   * <p>The general contract of the method {@code run} is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    int respawnField = 0;
    Random random = new Random(System.currentTimeMillis());
    Direction direction = Direction.get(random.nextInt(4));

    // Ai Level == 4, check, if the RespawnDirection has a wall
    Robot robot = player.getRobot();
    if (player.getPlayerType().ordinal() == 4) {
      FloorField floorField =
          gameDTO
              .getFloorPlan()
              .getFloorFields(robot.getPosition().getX(), robot.getPosition().getY());

      for (int i = 0; i < 4; i++) {
        Direction directionField = Direction.get(i);
        if (directionField != null && floorField.getNeighbour(directionField) != null) {
          direction = Direction.get(i);
          break;
        }
      }
    }
    if (onlyDirection) { // Only Direction from Robot
      bus.post(
          new RespawnDirectionInteractionRequest(gameDTO.getGameId(), player.getUser(), direction));
    } else { // Direction and Position from Robot
      for (int i = 0; i < floorFields.length; i++) {
        if (floorFields[i][0] != -1 && i != 4) {
          respawnField = i;
        }
      }
      bus.post(
          new RespawnInteractionRequest(
              gameDTO.getGameId(), player.getUser(), respawnField, direction));
    }
  }
}
