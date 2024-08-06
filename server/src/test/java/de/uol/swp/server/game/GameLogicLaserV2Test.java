package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.animation.Animation;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.ParallelAnimation;
import de.uol.swp.common.game.animation.SeparationPoint;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.Library;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.player.PlayerType;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * The GameLogicLaserV2Test class conducts unit tests for laserV2 function in the GameLogic class,
 *
 * <p>which is responsible for processing the game logic of a game.
 */
@SuppressWarnings("UnstableApiUsage")
class GameLogicLaserV2Test {

  User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(0));
  User user1 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(0));
  Robot testRobot;
  Robot testRobot1;
  Set<Player> players = new HashSet<>();

  FloorField field_RP; // Robot Position

  Game game = Mockito.mock(GameDTO.class);
  GameService gameService = Mockito.mock(GameService.class);
  GameLogic gameLogic = new GameLogic(game, gameService, new EventBus());

  //
  //	BasicFloorFieldIDs
  //
  String EMPTY_FIELD = "00";
  String PIT = "64";
  String PIT_CROSSED_LASERS_PASS = "72"; // all directions
  String LASER_SINGLE_SRC = "48"; // default: aims NORTH
  String LASER_SINGLE_PASS = "53"; // default: passes NORTH-SOUTH
  String LASER_SINGLE_PASS_EXPRESS = "84"; // default: passes EAST-WEST
  String LASER_SINGLE_SINK = "55"; // default: hits wall NORTH
  String LASER_DOUBLE_SRC = "49"; // default: aims NORTH
  String LASER_DOUBLE_SRC_WALL = "56"; // default: 2 laser SOUTH to Wall NORTH
  String LASER_DOUBLE_PASS = "54"; // default: passes NORTH-SOUTH
  String LASER_TRIPLE_SRC_WALL = "50"; // default: 3 laser SOUTH to Wall NORTH

  @BeforeEach
  void setup() {
    players.clear();
    //
    testRobot =
        new Robot(Robots.BOB, new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 0, 0));
    players.add(new Player(PlayerType.HUMAN_PLAYER, testRobot, user));
    testRobot1 =
        new Robot(Robots.OCEAN, new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 1, 0));
    players.add(new Player(PlayerType.HUMAN_PLAYER, testRobot1, user1));
    //
    doReturn(players).when(game).getPlayers();
  }

  @Test
  void laserV2Test_noDanger() {
    field_RP =
        new FloorField(
            Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5); // Direction, BasicFloorField, x, y
    assertRobotDamage(testRobot, 0, "No Lasers around");
    //
    //
    addFieldInDir(field_RP, Direction.NORTH, EMPTY_FIELD, Direction.NORTH);
    addFieldInDir(field_RP, Direction.EAST, EMPTY_FIELD, Direction.NORTH);
    addFieldInDir(field_RP, Direction.SOUTH, EMPTY_FIELD, Direction.NORTH);
    FloorField field_W1 = addFieldInDir(field_RP, Direction.WEST, EMPTY_FIELD, Direction.NORTH);
    assertRobotDamage(testRobot, 0, "No Lasers around 2");
    //
    //
    FloorField field_W2 = addFieldInDir(field_W1, Direction.WEST, EMPTY_FIELD, Direction.NORTH);
    FloorField field_W3 = addFieldInDir(field_W2, Direction.WEST, EMPTY_FIELD, Direction.NORTH);
    FloorField field_W4 = addFieldInDir(field_W3, Direction.WEST, EMPTY_FIELD, Direction.NORTH);
    assertRobotDamage(testRobot, 0, "No Lasers around 3");
    //
    //	Edge of FloorPlan
    FloorField edge = new FloorField(Direction.NORTH, Library.get(PIT), -1, -1);
    field_W4.setWest(edge);
    edge.setEast(field_W4);
    assertRobotDamage(testRobot, 0, "No Lasers around 4");
  }

  @Test
  void laserV2Test_onSource() {
    //  Single Laser Source
    for (Direction dir : Direction.values()) {
      field_RP = new FloorField(dir, Library.get(LASER_SINGLE_SRC), 5, 5);
      assertRobotDamage(testRobot, 1, "Laser Source hits independent of its direction, " + dir);
    }
    //  Double Laser Source between walls
    for (Direction dir : Direction.values()) {
      field_RP = new FloorField(dir, Library.get(LASER_DOUBLE_SRC_WALL), 5, 5);
      assertRobotDamage(
          testRobot, 2, "Double Laser Source hits independent of its direction, " + dir);
    }
    //  Triple Laser Source between walls
    for (Direction dir : Direction.values()) {
      field_RP = new FloorField(dir, Library.get(LASER_TRIPLE_SRC_WALL), 5, 5);
      assertRobotDamage(
          testRobot, 3, "Double Laser Source hits independent of its direction, " + dir);
    }
  }

  @Test
  void laserV2Test_nextToSource() {
    //  Single Laser Source
    for (Direction dir : Direction.values()) {
      field_RP = new FloorField(dir, Library.get(LASER_SINGLE_PASS), 5, 5);
      addFieldInDir(field_RP, dir, LASER_SINGLE_SRC, oppositeOf(dir));
      addFieldInDir(field_RP, oppositeOf(dir), LASER_SINGLE_SINK, oppositeOf(dir));
      assertRobotDamage(testRobot, 1, "Laser hits from " + dir);
    }
    //	Double Laser Source
    for (Direction dir : Direction.values()) {
      field_RP = new FloorField(dir, Library.get(LASER_DOUBLE_PASS), 5, 5);
      addFieldInDir(field_RP, dir, LASER_DOUBLE_SRC, oppositeOf(dir));
      assertRobotDamage(testRobot, 2, "Laser hits from " + dir);
    }
    //
    //	Laser marks at the robot position don't matter
    field_RP = new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5);
    addFieldInDir(field_RP, Direction.NORTH, LASER_SINGLE_SRC, Direction.SOUTH);
    assertRobotDamage(
        testRobot, 1, "Laser hits from NORTH, even if robot field is not marked with laser beam.");
  }

  @Test
  void laserV2Test_farFromSource() {
    field_RP = new FloorField(Direction.NORTH, Library.get(LASER_SINGLE_PASS), 5, 5);
    addFieldInDir(field_RP, Direction.NORTH, LASER_SINGLE_SINK, Direction.NORTH);
    FloorField field_S1 =
        addFieldInDir(field_RP, Direction.SOUTH, LASER_SINGLE_PASS, Direction.NORTH);
    FloorField field_S2 =
        addFieldInDir(field_S1, Direction.SOUTH, LASER_SINGLE_PASS, Direction.NORTH);
    FloorField field_S3 =
        addFieldInDir(field_S2, Direction.SOUTH, LASER_SINGLE_PASS, Direction.NORTH);
    addFieldInDir(field_S3, Direction.SOUTH, LASER_SINGLE_SRC, Direction.NORTH);
    assertRobotDamage(testRobot, 1, "Laser hits from South far away");
    //
    assertLaserDamageAnimation(testRobot, new int[] {1});
    //
    // other robot blocks the way
    gameLogic = new GameLogic(game, gameService, new EventBus());
    testRobot1.resetDamage();
    testRobot1.setPosition(field_S2);
    assertRobotDamage(testRobot, 0, "Laser fires from South but other robot blocks the way.");
    assertEquals(1, testRobot1.getDamage(), "Laser hits the blocking robot from South.");
    //
    assertLaserDamageAnimationTwoRobots(testRobot, 0, testRobot1, 1);
  }

  @Test
  void laserV2Test_visibleButNotHitting() {
    //  Single Laser Sources, in every direction two, but none pointing at robot
    field_RP = new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5);
    for (Direction dir : Direction.values()) {
      FloorField field_D1 =
          addFieldInDir(
              field_RP, dir, LASER_SINGLE_SRC, dir.rotate(Direction.EAST)); // turned right
      addFieldInDir(field_D1, dir, LASER_SINGLE_SRC, dir.rotate(Direction.WEST)); // turned left
    }
    assertRobotDamage(testRobot, 0, "None of the lasers hits.");
    //
    // Double laser against wall
    field_RP =
        new FloorField(
            Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5); // like GameLogicTest::laserTest FP#1
    addFieldInDir(field_RP, Direction.NORTH, LASER_DOUBLE_SRC_WALL, Direction.EAST);
    assertRobotDamage(testRobot, 0, "Laser North passes by");
  }

  @Test
  void laserV2Test_multipleHits() {
    field_RP = new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5);
    //
    FloorField field_N1 =
        addFieldInDir(field_RP, Direction.NORTH, LASER_SINGLE_PASS, Direction.SOUTH);
    addFieldInDir(field_N1, Direction.NORTH, LASER_SINGLE_SRC, Direction.SOUTH);
    assertRobotDamage(testRobot, 1, "One lasers hits.");
    //
    FloorField field_E1 =
        addFieldInDir(field_RP, Direction.EAST, LASER_SINGLE_PASS, Direction.WEST);
    FloorField field_E2 =
        addFieldInDir(
            field_E1,
            Direction.EAST,
            LASER_SINGLE_PASS_EXPRESS,
            Direction.NORTH); // express has different orientation!
    addFieldInDir(field_E2, Direction.EAST, LASER_SINGLE_SRC, Direction.WEST);
    assertRobotDamage(testRobot, 2, "Second lasers hits.");
    //
    FloorField field_S1 =
        addFieldInDir(field_RP, Direction.SOUTH, LASER_SINGLE_PASS, Direction.NORTH);
    FloorField field_S2 =
        addFieldInDir(field_S1, Direction.SOUTH, LASER_SINGLE_PASS, Direction.NORTH);
    addFieldInDir(field_S2, Direction.SOUTH, LASER_SINGLE_SRC, Direction.NORTH);
    assertRobotDamage(testRobot, 3, "Third lasers hits.");
    //
    FloorField field_W1 =
        addFieldInDir(field_RP, Direction.WEST, LASER_SINGLE_PASS, Direction.EAST);
    addFieldInDir(field_W1, Direction.WEST, LASER_DOUBLE_SRC, Direction.EAST);
    assertRobotDamage(testRobot, 5, "Fourth DOUBLE lasers hits.");
    //
    assertLaserDamageAnimation(testRobot, new int[] {1, 2, 3, 5});
    //
    //
    // other robot blocks one way
    gameLogic = new GameLogic(game, gameService, new EventBus());
    testRobot1.resetDamage();
    testRobot1.setPosition(field_S2);
    assertRobotDamage(testRobot, 4, "Four Lasers hit and other robot blocks one laser.");
    assertEquals(1, testRobot1.getDamage(), "Laser hits the blocking robot from South.");
    //
    assertLaserDamageAnimationTwoRobots(testRobot, 4, testRobot1, 1);
  }

  @Test
  void laserV2Test_noHitsAroundCorners() {
    field_RP = new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 5, 5);
    //
    FloorField field_E1 =
        addFieldInDir(field_RP, Direction.EAST, LASER_SINGLE_PASS, Direction.WEST);
    FloorField field_E2 =
        addFieldInDir(field_E1, Direction.EAST, LASER_SINGLE_PASS, Direction.WEST);
    addFieldInDir(field_E2, Direction.EAST, LASER_SINGLE_SRC, Direction.WEST);
    //
    FloorField field_W1 =
        addFieldInDir(field_RP, Direction.WEST, PIT_CROSSED_LASERS_PASS, Direction.WEST);
    addFieldInDir(field_W1, Direction.SOUTH, LASER_DOUBLE_SRC, Direction.NORTH);
    assertRobotDamage(testRobot, 1, "Only one laser hits."); // no hit around corner
  }

  // -------------------------------------------------------------------
  //	Helper Methods
  // -------------------------------------------------------------------

  /*
   *	Assertion Methods
   *	  for improving test readability
   */

  // Damage
  private void assertRobotDamage(Robot robot, Integer hits, String info) {
    robot.resetDamage();
    robot.setPosition(field_RP);
    gameLogic.laserV2();
    assertEquals(hits, robot.getDamage(), info);
  }

  // Animation
  private void assertLaserDamageAnimation(Robot robot, int[] nbAnims) {
    List<Animation> steps = gameLogic.getStep().getSteps();
    assertEquals(nbAnims.length, steps.size());
    int idx = 0;
    for (Animation anim : steps) {
      assertParallelAnimation(anim);
      List<Animation> subSteps = ((ParallelAnimation) anim).getAnimations();
      assertEquals(nbAnims[idx] * 2, subSteps.size());
      for (int i = 0; i < nbAnims[idx]; i++) {
        assertSingleLaserDamageAnimation(subSteps.get(2 * i), robot);
        assertSeparationPointAnimation(subSteps.get(2 * i + 1), robot);
      }
      idx++;
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void assertLaserDamageAnimationTwoRobots(
      Robot robot1, int nbAnims1, Robot robot2, int nbAnims2) {
    List<Animation> steps = gameLogic.getStep().getSteps();
    assertEquals(1, steps.size(), "All Animations in 1 parallel");
    Animation anim = steps.get(0);
    assertParallelAnimation(anim);
    List<Animation> subSteps = ((ParallelAnimation) anim).getAnimations();
    //
    int cntAnims = 0;
    int cntAnims1 = 0;
    int cntAnims2 = 0;
    for (int i = 0; i < subSteps.size(); i += 2) {
      Animation subAnim = subSteps.get(i);
      if (robot1.getType() == subAnim.getRobot()) {
        assertSingleLaserDamageAnimation(subAnim, robot1);
        assertSeparationPointAnimation(subSteps.get(i + 1), robot1);
        cntAnims1++;
      } else if (robot2.getType() == subAnim.getRobot()) {
        assertSingleLaserDamageAnimation(subAnim, robot2);
        assertSeparationPointAnimation(subSteps.get(i + 1), robot2);
        cntAnims2++;
      }
      cntAnims++;
    }
    assertEquals(nbAnims1 + nbAnims2, cntAnims, "Number of Animations combined");
    assertEquals(nbAnims1, cntAnims1, "Number of Animations Robot#1");
    assertEquals(nbAnims2, cntAnims2, "Number of Animations Robot#2");
  }

  private void assertParallelAnimation(Animation anim) {
    assertTrue(anim instanceof ParallelAnimation);
    assertNull(anim.getType());
    assertNull(anim.getRobot());
    assertNull(anim.getDirection());
    assertTrue(anim.isParallel());
  }

  private void assertSingleLaserDamageAnimation(Animation anim, Robot robot) {
    assertTrue(anim instanceof SingleAnimation);
    assertEquals(AnimationType.LASER_DAMAGE, anim.getType());
    assertEquals(robot.getType(), anim.getRobot());
    assertEquals(robot.getDirection(), anim.getDirection());
    assertFalse(anim.isParallel());
  }

  private void assertSeparationPointAnimation(Animation anim, Robot robot) {
    assertTrue(anim instanceof SeparationPoint);
    assertEquals(AnimationType.UPDATE_DAMAGE_AND_LIVES, anim.getType());
    assertEquals(robot.getType(), anim.getRobot());
    assertNull(anim.getDirection());
    assertFalse(anim.isParallel());
  }

  /*
   *	Helper Methods
   *	 for creating test factory floors
   */

  private Direction oppositeOf(Direction inDir) {
    return inDir.rotate(Direction.SOUTH);
  }

  private FloorField addFieldInDir(
      FloorField oldField,
      Direction addDirection,
      String newBasicFloorFieldId,
      Direction newFieldDir) {
    FloorField newField;
    switch (addDirection) {
      case NORTH:
        newField =
            new FloorField(
                newFieldDir,
                Library.get(newBasicFloorFieldId), // Direction, BasicFloorField, x, y
                oldField.getX(),
                oldField.getY() - 1);
        oldField.setNorth(newField);
        newField.setSouth(oldField);
        return newField;
      case EAST:
        newField =
            new FloorField(
                newFieldDir,
                Library.get(newBasicFloorFieldId),
                oldField.getX() + 1,
                oldField.getY());
        oldField.setEast(newField);
        newField.setWest(oldField);
        return newField;
      case SOUTH:
        newField =
            new FloorField(
                newFieldDir,
                Library.get(newBasicFloorFieldId),
                oldField.getX(),
                oldField.getY() + 1);
        oldField.setSouth(newField);
        newField.setNorth(oldField);
        return newField;
      case WEST:
        newField =
            new FloorField(
                newFieldDir,
                Library.get(newBasicFloorFieldId),
                oldField.getX() - 1,
                oldField.getY());
        oldField.setWest(newField);
        newField.setEast(oldField);
        return newField;
      default:
        throw new IllegalArgumentException("Direction does not exist");
    }
  }
}
