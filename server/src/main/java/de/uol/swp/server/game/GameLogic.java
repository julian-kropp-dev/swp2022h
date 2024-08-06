package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.ParallelAnimation;
import de.uol.swp.common.game.animation.SeparationPoint;
import de.uol.swp.common.game.animation.SingleAnimation;
import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.game.dto.Step.RobotInformation;
import de.uol.swp.common.game.floor.BasicFloorField;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.Operation;
import de.uol.swp.common.game.floor.operation.Belt;
import de.uol.swp.common.game.floor.operation.Checkpoint;
import de.uol.swp.common.game.floor.operation.ExpressBelt;
import de.uol.swp.common.game.floor.operation.Laser;
import de.uol.swp.common.game.floor.operation.Press;
import de.uol.swp.common.game.floor.operation.Pusher;
import de.uol.swp.common.game.floor.operation.Repair;
import de.uol.swp.common.game.message.WinMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.player.PlayerType;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.message.GameLogicFinish;
import de.uol.swp.server.message.ServerInternalMessage;
import de.uol.swp.server.message.UpdateUserStatsMessage;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class evaluates the game using the rules of the manual.
 *
 * <p>The reference refers to the German game instructions.
 */
@SuppressWarnings("UnstableApiUsage")
public class GameLogic implements Runnable {
  private static final Logger LOG = LogManager.getLogger(GameLogic.class);
  private final Game game;
  private final EventBus bus;
  GameService gameService;
  private Step step;
  private ParallelAnimation tempParallelAnimation = new ParallelAnimation();

  /**
   * Default constructor.
   *
   * @param game The game that is to be evaluated.
   * @param bus The server's bus.
   */
  public GameLogic(Game game, GameService gameService, EventBus bus) {
    this(game, gameService, bus, false);
  }

  /**
   * Constructor.
   *
   * @param game The game that is to be evaluated.
   * @param bus The server's bus.
   * @param testMode true for UnitTests
   */
  @SuppressWarnings("unused")
  public GameLogic(Game game, GameService gameService, EventBus bus, boolean testMode) {
    this.bus = bus;
    bus.register(this);
    this.game = game;
    this.gameService = gameService;
    setup();
  }

  /**
   * Getter for the game needed for testing purposes.
   *
   * @return the game
   */
  public Game getGame() {
    return game;
  }

  public Step getStep() {
    return step;
  }

  /** Only for Commands. */
  public void setup() {
    this.step = new Step(game.getGameId());
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
    setup();
    for (int i = 0; i < 5; i++) {
      step.addAnimation(new SeparationPoint(AnimationType.MOVEMENT));
      moveRobots(i);
      factoryPhase(i);
      if (Thread.currentThread().isInterrupted()) {
        return;
      }
    }
    step.addAnimation(new SeparationPoint(AnimationType.REPAIR_FIELD));
    repair();
    if (game.getLobby().getOptions().isSwitchOffRoboter()) {
      shutdown();
    }

    setQueueForRespawn();

    ServerInternalMessage msg = new GameLogicFinish(step, game.getLobby());
    bus.post(msg);
  }

  /**
   * The method for moving the Robot corresponds to the instructions in III.a in the program
   * sequence. This is where the commands are called. The order in which the commands are executed
   * is also specified here.
   *
   * @param programStep Is the program step.
   */
  public void moveRobots(int programStep) {
    List<Robot> robots =
        game.getPlayers().stream()
            .map(Player::getRobot)
            .filter(Robot::isAlive)
            .collect(Collectors.toList());
    List<ProgrammingCard> temp = new ArrayList<>();
    for (Robot robot : robots) {
      if (robot.getSelectedCards().length > programStep
          && robot.getSelectedCards()[programStep] != null) {
        temp.add(robot.getSelectedCards()[programStep]);
      }
    }
    temp.sort((c1, c2) -> (c2.getCount() - c1.getCount())); // reverse order
    ParallelAnimation tempLocal = new ParallelAnimation();
    for (ProgrammingCard card : temp) {
      if (card != null) {
        tempLocal.addAnimation(
            new SeparationPoint(
                AnimationType.DISPLAY_NEXT_CARD,
                card.getCardType(),
                card.getCount(),
                card.getOwner()));
      }
    }
    step.addAnimation(tempLocal);
    for (ProgrammingCard card : temp) {
      if (card != null) {
        Optional<Player> ownerOptional =
            game.getPlayers().stream()
                .filter(player -> player.getRobot().getType() == card.getOwner())
                .findFirst();
        ownerOptional.ifPresentOrElse(
            player -> {
              LOG.trace(
                  "ProgramCard#{}: Robot {} plays card {} ({})",
                  programStep,
                  player.getRobot().getType(),
                  card.getCardType(),
                  card.getCount());
              processingCards(card.getCardType(), player.getRobot());
            },
            () -> LOG.error("Card is not present"));
      }
    }
  }

  /**
   * This method corresponds to part 3 of the program sequence III.b in the instructions. The
   * individual points of the factory phase are called up here.
   *
   * @param programmStep Is the program step.
   */
  public void factoryPhase(int programmStep) {
    step.addAnimation(new SeparationPoint(AnimationType.getAnimationForFactoryPhase(programmStep)));
    programmStep++;
    tempParallelAnimation = new ParallelAnimation();
    tempParallelAnimation.addAnimation(
        new SeparationPoint(AnimationType.FACTORY_PHASE_EXPRESS_ONE));
    expressOne();

    tempParallelAnimation = new ParallelAnimation();
    tempParallelAnimation.addAnimation(
        new SeparationPoint(AnimationType.FACTORY_PHASE_EXPRESS_TWO_AND_NORMAL));
    expressTwoAndNormalBelts();

    step.addAnimation(
        new SeparationPoint(AnimationType.getAnimationTypeForPusher(programmStep - 1)));
    pusher(programmStep);

    step.addAnimation(new SeparationPoint(AnimationType.FACTORY_PHASE_GEARS));
    gears();

    step.addAnimation(
        new SeparationPoint(AnimationType.getAnimationTypeForPress(programmStep - 1)));
    presses(programmStep);

    step.addAnimation(new SeparationPoint(AnimationType.FACTORY_PHASE_LASER));
    laser();
    // Variant I active
    if (game.getLobby().getOptions().isActiveLasers()) {
      step.addAnimation(new SeparationPoint(AnimationType.FACTORY_PHASE_ROBOT_LASER));
      robotLaser();
    }
  }

  /**
   * This method corresponds to III.b.1
   *
   * <p>In this method, the express belt is evaluated.
   */
  public void expressOne() {
    Map<Robot, FloorField> desiredFloorFields = new HashMap<>();
    game.getPlayers().stream()
        .map(Player::getRobot)
        .filter(Robot::isAlive)
        .forEach(
            robot -> {
              FloorField floorField = robot.getPosition();
              desiredFloorFields.put(robot, floorField);
              floorField
                  .getBasicFloorField()
                  .getOperations()
                  .forEach(
                      (z, x) -> {
                        if (x instanceof ExpressBelt) {
                          desiredFloorFields.put(
                              robot,
                              floorField.getNeighbour(
                                  x.getDirections()
                                      .get(0)
                                      .rotate(robot.getPosition().getDirection())));
                        }
                      });
            });
    doTheBelts(desiredFloorFields);
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  /**
   * This method corresponds to III.b.2
   *
   * <p>In this method, the express belts and the conveyor belts are evaluated.
   */
  public void expressTwoAndNormalBelts() {
    Map<Robot, FloorField> desiredFloorFields = new HashMap<>();
    game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .forEach(
            robot -> {
              FloorField floorField = robot.getPosition();
              desiredFloorFields.put(robot, floorField);
              floorField
                  .getBasicFloorField()
                  .getOperations()
                  .forEach(
                      (z, x) -> {
                        if (x instanceof Belt || x instanceof ExpressBelt) {
                          desiredFloorFields.put(
                              robot,
                              floorField.getNeighbour(
                                  x.getDirections()
                                      .get(0)
                                      .rotate(robot.getPosition().getDirection())));
                        }
                      });
            });
    doTheBelts(desiredFloorFields);
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  @SuppressWarnings({"java:S6541", "java:S3776"})
  private void doTheBelts(Map<Robot, FloorField> desiredFloorFields) {
    boolean curve = false;
    Direction directionOfFloorField = Direction.NORTH;
    Direction directionOfTargetFloorField = Direction.NORTH;

    Collection<FloorField> floorFields = desiredFloorFields.values();
    List<FloorField> forbiddenFields = new LinkedList<>();
    for (FloorField field : floorFields) {
      int counter = 0;
      for (FloorField otherField : floorFields) {
        if (field.equals(otherField)) {
          counter++;
        }
      }
      if (counter > 1) {
        forbiddenFields.add(field);
      }
    }

    for (Entry<Robot, FloorField> entry : desiredFloorFields.entrySet()) {
      BasicFloorField currentField = entry.getKey().getPosition().getBasicFloorField();
      FloorField desiredFloorField = entry.getValue();
      BasicFloorField desiredBasicFloorField = desiredFloorField.getBasicFloorField();
      FloorField currentFloorField = entry.getKey().getPosition();

      Robot robot = entry.getKey();
      if (entry.getKey().getPosition() != entry.getValue()) {
        if (currentField.getOperations().get(0) != null) {
          directionOfFloorField =
              currentField
                  .getOperations()
                  .get(0)
                  .getDirections()
                  .get(0)
                  .rotate(currentFloorField.getDirection());
        }
        if (currentField.getOperations().get(1) != null) {
          directionOfFloorField =
              currentField
                  .getOperations()
                  .get(1)
                  .getDirections()
                  .get(0)
                  .rotate(currentFloorField.getDirection());
        }
        if (desiredBasicFloorField.getOperations().get(0) != null) { // 0 is Express

          directionOfTargetFloorField =
              desiredBasicFloorField
                  .getOperations()
                  .get(0)
                  .getDirections()
                  .get(0)
                  .rotate(desiredFloorField.getDirection());
        } else if (desiredBasicFloorField.getOperations().get(1) != null) { // 1 is normal Belt
          directionOfTargetFloorField =
              desiredBasicFloorField
                  .getOperations()
                  .get(1)
                  .getDirections()
                  .get(0)
                  .rotate(desiredFloorField.getDirection());
        }

        if (!(forbiddenFields.contains(desiredFloorField))) {
          if (desiredFloorField.getBasicFloorField().getOperations().containsKey(-1)) {
            tempParallelAnimation.addAnimation(
                new SingleAnimation(
                    AnimationType.PIT,
                    robot.getType(),
                    robot.getPosition().getX(),
                    robot.getPosition().getY(),
                    robot.getDirection()));
            destroy(robot);
          } else {
            tempParallelAnimation.addAnimation(
                new SingleAnimation(
                    AnimationType.getAnimationForBelt(directionOfFloorField),
                    robot.getType(),
                    robot.getPosition().getX(),
                    robot.getPosition().getY(),
                    robot.getDirection()));
            robot.setPosition(desiredFloorField);
            robot.setLastMoveByBelt(true);
          }
        }

        if (directionOfFloorField != directionOfTargetFloorField) {
          curve = true;
        }
      }

      if (curve) {
        doTheCurves(
            robot, desiredBasicFloorField, directionOfFloorField, directionOfTargetFloorField);
      }
      robot.setLastMoveByBelt(false);
    }
  }

  @SuppressWarnings("DataFlowIssue")
  private void doTheCurves(
      Robot robot,
      BasicFloorField desiredBasicFloorField,
      Direction directionCurrentField,
      Direction directionDesiredFloorField) {
    if (robot.isLastMoveByBelt()) {
      desiredBasicFloorField
          .getOperations()
          .forEach(
              (z, x) -> {
                if (x instanceof ExpressBelt || x instanceof Belt) {
                  int dir = directionDesiredFloorField.ordinal() - directionCurrentField.ordinal();
                  Direction targetDirection =
                      Direction.get(Math.floorMod(robot.getDirection().ordinal() + dir, 4));
                  tempParallelAnimation.addAnimation(
                      new SingleAnimation(
                          AnimationType.calculateTurningDir(robot.getDirection(), targetDirection),
                          robot.getType(),
                          robot.getPosition().getX(),
                          robot.getPosition().getY(),
                          robot.getDirection()));
                  robot.setDirection(targetDirection);
                }
              });
    }
  }

  /**
   * This method corresponds to III.b.3
   *
   * <p>This method evaluates the sliders.
   *
   * @param programmStep Is the program step.
   */
  public void pusher(int programmStep) {
    tempParallelAnimation = new ParallelAnimation();
    game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .forEach(
            robot -> {
              try {
                Pusher pusher =
                    (Pusher) robot.getPosition().getBasicFloorField().getOperations().get(2);
                if (pusher.isInProgramStep(programmStep)) {
                  move(
                      robot,
                      pusher
                          .getDirections()
                          .get(0)
                          .rotate(robot.getPosition().getDirection())
                          .rotate(Direction.SOUTH));
                }
              } catch (NullPointerException e) {
                // FloorField has no pusher
              }
            });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  /**
   * This method corresponds to III.b.4
   *
   * <p>In this method the gears are evaluated.
   */
  public void gears() {
    tempParallelAnimation = new ParallelAnimation();
    game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .forEach(
            robot -> {
              if (robot.getPosition().getBasicFloorField().getId().equals("37")) {
                tempParallelAnimation.addAnimation(
                    new SingleAnimation(
                        AnimationType.TURN_LEFT,
                        robot.getType(),
                        robot.getPosition().getX(),
                        robot.getPosition().getY(),
                        robot.getDirection()));
                robot.setDirection(Direction.get((robot.getDirection().ordinal() - 1 + 4) % 4));
              }
              if (robot.getPosition().getBasicFloorField().getId().equals("38")) {
                tempParallelAnimation.addAnimation(
                    new SingleAnimation(
                        AnimationType.TURN_RIGHT,
                        robot.getType(),
                        robot.getPosition().getX(),
                        robot.getPosition().getY(),
                        robot.getDirection()));
                robot.setDirection(Direction.get((robot.getDirection().ordinal() + 1) % 4));
              }
            });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  /**
   * This method corresponds to III.b.5
   *
   * <p>In this method the presses are evaluated.
   *
   * @param programStep Is the program step.
   */
  private void presses(int programStep) {
    tempParallelAnimation = new ParallelAnimation();
    game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .forEach(
            r -> {
              try {
                Press press = (Press) r.getPosition().getBasicFloorField().getOperations().get(4);
                if (press.isInProgramStep(programStep)) {
                  tempParallelAnimation.addAnimation(
                      new SingleAnimation(
                          AnimationType.PRESS,
                          r.getType(),
                          r.getPosition().getX(),
                          r.getPosition().getY(),
                          r.getDirection()));
                  destroy(r);
                }
              } catch (NullPointerException e) {
                // FloorField has no press
              }
            });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }



  /**
   * This method corresponds to III.b.6
   *
   * <p>In this method the lasers are evaluated.
   */
  public void laser() {
    laserV2();
  }

  /**
   * This method corresponds to III.b.6 Version#1
   *
   * <p>V1: This method interprets the rules in the following way: Laser beams are only hitting
   * fields which are visibly marked with a laser beam and intensity is defined by the number of
   * beams shown on the field itself.
   *
   * <p>Lasers which point to this field, but whose beam is not visible on the field are not
   * counted. Laser from opposite directions which share a beam are counted only once.
   *
   * <p>On graphically correct designed factory floors there is no difference between version#1 and
   * version#2. Only version#2 allows the change of factory floors without creating new images for
   * fields crossed by laser beam.
   *
   * <p>WARNING: This implementation has several bugs which are very difficult to fix. Some of these
   * bugs may also be found in other parts of GameLogic.
   * <li>firing direction of laser source is not checked and FieldRotation is not applied.
   * <li>when laser beams cross a robot can be hit by a source around the corner.
   * <li>if the laser beam hits a wall behind a robot, it may not damage the robot.
   * <li>the reason here is that the NullPointerException catches too much, in this case the missing
   *     of a neighbour field because of the wall. This problem may exist in other methods in this
   *     class too.
   */
  @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
  public void laserV1() {
    LinkedList<FloorField> robotPositions = new LinkedList<>();
    game.getPlayers().forEach(p -> robotPositions.add(p.getRobot().getPosition()));
    tempParallelAnimation = new ParallelAnimation();
    game.getPlayers()
        .forEach(
            p -> {
              try {
                Robot robot = p.getRobot();
                FloorField floorField = robot.getPosition();
                LinkedList<FloorField> otherRobotsPositions = new LinkedList<>();
                game.getPlayers()
                    .forEach(
                        t -> {
                          if (!(t.equals(p))) {
                            otherRobotsPositions.add(t.getRobot().getPosition());
                          }
                        });
                floorField
                    .getBasicFloorField()
                    .getOperations()
                    .forEach(
                        (k, o) -> {
                          if (hitbyLaser(robot.getPosition(), otherRobotsPositions)) {
                            for (int i = 0; i < ((Laser) o).getIntensity(); i++) {
                              tempParallelAnimation.addAnimation(
                                  new SingleAnimation(
                                      AnimationType.LASER_DAMAGE,
                                      robot.getType(),
                                      robot.getPosition().getX(),
                                      robot.getPosition().getY(),
                                      robot.getDirection()));
                              damage(robot);
                              tempParallelAnimation.addAnimation(
                                  new SeparationPoint(
                                      AnimationType.UPDATE_DAMAGE_AND_LIVES,
                                      robot.getType(),
                                      robot.getLives(),
                                      robot.getDamage()));
                            }
                          }
                        });
              } catch (NullPointerException e) {
                // FloorField has no laser
              }
            });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  private boolean hitbyLaser(FloorField field, LinkedList<FloorField> otherRobotsPositions) {
    LinkedList<Direction> laserDirections = new LinkedList<>();
    Laser laser = null;
    boolean islaser = false;
    for (Operation op : field.getBasicFloorField().getOperations().values()) {
      if (op instanceof Laser) {
        islaser = true;
        laser = (Laser) op;
        laserDirections.addAll(op.getDirections());
      }
    }
    if (islaser && !(otherRobotsPositions.contains(field)) && laser.isSource()) {
      return true;
    } else if (islaser && !(otherRobotsPositions.contains(field))) {
      boolean back = false;
      for (Direction dir : laserDirections) {
        back = back || hitbyLaser(field.getNeighbour(dir), otherRobotsPositions);
      }
      return back;
    }
    return false;
  }

  /**
   * This method corresponds to III.b.6 Version#2
   *
   * <p>V2: This method interprets the rules in the following way: Laser beams hit fields
   * independently of what is visible on the field. All laser sources which point at this field and
   * can reach it are counted no matter how many beams are visible on this field and which direction
   * they have.
   *
   * <p>On graphically correct designed factory floors there is no difference between version#1 and
   * version#2. Only version#2 allows the change of factory floors without creating new images for
   * fields crossed by laser beam.
   *
   * <p>In this method the lasers are evaluated.
   */
  @SuppressWarnings({"checkstyle:LocalVariableName", "java:S3776"})
  public void laserV2() {
    Map<Robot, FloorField> robotPositions = new HashMap<>();
    for (Player p : game.getPlayers()) {
      Robot r = p.getRobot();
      FloorField rPos = r.getPosition();
      if (rPos != null) {
        robotPositions.put(r, rPos);
      }
    }
    tempParallelAnimation = new ParallelAnimation();
    robotPositions.forEach(
        (robot, robotPos) -> {
          int laserHits = hitsByLaserFrom(robotPos, null);
          for (Direction dir : Direction.values()) {
            // Depends on the fact that laser beams can only
            // come from walls, that is from fields from which
            // there is no further way in this direction.
            FloorField wallField = robotPos.getNeighbour(dir);
            FloorField next = wallField;
            while (next != null) {
              if ((next.getX() < 0) || robotPositions.containsValue(next)) {
                // meets robot/border before any laser source
                // => no danger from this direction
                wallField = null;
                break;
              }
              wallField = next;
              next = wallField.getNeighbour(dir);
            }
            if (wallField != null) {
              laserHits += hitsByLaserFrom(wallField, dir);
            }
          }
          for (int i = 0; i < laserHits; i++) {
            tempParallelAnimation.addAnimation(
                new SingleAnimation(
                    AnimationType.LASER_DAMAGE,
                    robot.getType(),
                    robotPos.getX(),
                    robotPos.getY(),
                    robot.getDirection()));
            damage(robot);
            tempParallelAnimation.addAnimation(
                new SeparationPoint(
                    AnimationType.UPDATE_DAMAGE_AND_LIVES,
                    robot.getType(),
                    robot.getLives(),
                    robot.getDamage()));
            if (robot.getPosition() == null) {
              break;
            }
          }
        });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  protected int hitsByLaserFrom(FloorField field, Direction fromDir) {
    for (Operation op : field.getBasicFloorField().getOperations().values()) {
      if ((op instanceof Laser) && (((Laser) op).isSource())) {
        for (Direction d : op.getDirections()) {
          Direction firesFrom = d.rotate(field.getDirection());
          if ((fromDir == null) || (firesFrom == fromDir)) {
            return ((Laser) op).getIntensity();
          }
        }
      }
    }
    return 0;
  }

  /** This method corresponds to VARIANT.I */
  public void robotLaser() {
    tempParallelAnimation = new ParallelAnimation();
    game.getPlayers().stream()
        .filter(player -> !(player.getRobot().isShutdown()))
        .forEach(
            t -> {
              Robot robot = t.getRobot();
              List<Robot> allRobots = new ArrayList<>();
              game.getPlayers().forEach(r -> allRobots.add(r.getRobot()));
              allRobots.remove(t.getRobot());
              FloorField floorField = robot.getPosition();
              floorField.getNeighbour(robot.getDirection());
              recursiveNeighbour(floorField, robot.getDirection(), allRobots);
            });
    if (!tempParallelAnimation.getAnimations().isEmpty()) {
      step.addAnimation(tempParallelAnimation);
    }
    tempParallelAnimation = new ParallelAnimation();
  }

  /** This method corresponds to VARIANT.III */
  public void weakRobotDuplicate(Robot robot) {
    robot.alive();
    damage(robot);
    damage(robot);
  }

  /** private recursive Method to check if the robotLaser hits another robot. */
  @SuppressWarnings("java:S2583")
  private FloorField recursiveNeighbour(
      FloorField floorField, Direction direction, List<Robot> allRobots) {
    FloorField floorFieldNeighbour = floorField.getNeighbour(direction);
    if (floorFieldNeighbour == null
        || floorFieldNeighbour.getBasicFloorField().getId().equals("64")) {
      return null;
    }
    boolean end = false;

    for (Robot robot : allRobots) {
      if (robot.getPosition().equals(floorFieldNeighbour)) {
        tempParallelAnimation.addAnimation(
            new SingleAnimation(
                AnimationType.LASER_DAMAGE,
                robot.getType(),
                robot.getPosition().getX(),
                robot.getPosition().getY(),
                robot.getDirection()));
        damage(robot);
        tempParallelAnimation.addAnimation(
            new SeparationPoint(
                AnimationType.UPDATE_DAMAGE_AND_LIVES,
                robot.getType(),
                robot.getLives(),
                robot.getDamage()));
        end = true;
        break;
      }
    }
    if (end) {
      return null;
    }
    return recursiveNeighbour(floorFieldNeighbour, direction, allRobots);
  }

  /**
   * In this method, a command is broken down into its component parts, e.g. 3 steps forward becomes
   * 3 times one step forward. In addition, the special cases are intercepted and evaluated. Another
   * method is called for evaluation when a robot is pushed.
   *
   * @param card the card to process
   * @param robot the robot that the card is played on
   */
  public void processingCards(CardType card, Robot robot) {
    if (card.getSteps() == 0) { // Turn and U-Turn
      step.addAnimation(
          new SingleAnimation(
              AnimationType.getAnimationForTurning(card.getDirection()),
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
      robot.setDirection(robot.getDirection().rotate(card.getDirection()));
      return;
    }

    Direction direction;
    int steps;
    if (card.getSteps() == -1) { // Backup
      direction = robot.getDirection().rotate(Direction.SOUTH);
      steps = 1;
    } else { // Forward
      direction = robot.getDirection();
      steps = card.getSteps();
    }
    for (int i = 0; i < steps; i++) {
      if (robot.isAlive()) {
        tempParallelAnimation = new ParallelAnimation();
        move(robot, direction);
        if (!tempParallelAnimation.getAnimations().isEmpty()) {
          step.addAnimation(tempParallelAnimation);
        }
        tempParallelAnimation = new ParallelAnimation();
      }
    }
  }

  /**
   * this methode is for CommandPurposes only. is start the Move methode and return the Step object;
   *
   * @param robot the robot to move
   * @param direction the direction to move the robot
   * @return the animationList
   */
  public ParallelAnimation moveRobotExtern(Robot robot, Direction direction) {
    tempParallelAnimation = new ParallelAnimation();
    move(robot, direction);
    return tempParallelAnimation;
  }

  private boolean move(Robot robot, Direction direction) {
    FloorField nextPos = robot.getPosition().getNeighbour(direction);
    if (nextPos == null) { // Robot hits wall
      tempParallelAnimation.addAnimation(
          new SingleAnimation(
              AnimationType.getAnimationForWall(direction),
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
      return false;
    }
    if (nextPos.getBasicFloorField().getOperations().containsKey(-1)) { // Robot falls in pit
      tempParallelAnimation.addAnimation(
          new SingleAnimation(
              AnimationType.PIT,
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              direction));
      try {
        destroy(robot);
      } catch (NoSuchElementException e) {
        throw new IllegalArgumentException("Player not found");
      }
      return true;
    }
    Optional<Robot> opt =
        game.getPlayers().stream()
            .map(Player::getRobot)
            .filter(Robot::isAlive)
            .filter(r -> r.getPosition().equals(nextPos) && !r.equals(robot))
            .findAny();

    if (opt.isPresent() && !move(opt.get(), direction)) { // Robot pushes another robot
      return false;
    }

    robot.setPosition(nextPos);
    tempParallelAnimation.addAnimation(
        new SingleAnimation(
            AnimationType.getAnimationForDriving(direction),
            robot.getType(),
            robot.getPosition().getX(),
            robot.getPosition().getY(),
            robot.getDirection()));
    if (nextPos.getBasicFloorField().getOperations().containsKey(6) && !robot.isShutdown()) {
      robot.setRespawn(nextPos); // Robot updates archive
      game.changeRobotOnRespawnFieldOrder(nextPos, robot);
      tempParallelAnimation.addAnimation(
          new SingleAnimation(
              AnimationType.ARCHIVE,
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
    } else if (nextPos.getBasicFloorField().getOperations().containsKey(7) && !robot.isShutdown()) {
      robot.setRespawn(nextPos); // Robot updates archive
      int number = ((Checkpoint) nextPos.getBasicFloorField().getOperations().get(7)).getNumber();
      if (robot.getCurrentCheckpoint() + 1 == number) {
        robot.setCurrentCheckpoint(number);
        checkWinnerCheckpoints(robot);
      }
      tempParallelAnimation.addAnimation(
          new SingleAnimation(
              AnimationType.CHECKPOINT,
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection(),
              robot.getCurrentCheckpoint()));
      game.changeRobotOnRespawnFieldOrder(nextPos, robot);
    }
    return true;
  }

  /** Set the Queue for the respawn of the dead robots. */
  public void setQueueForRespawn() {
    // The Queue of the Robots that need to respawn in the order of the FloorField
    Map<FloorField, Deque<Robot>> queue = new HashMap<>();

    Map<FloorField, List<Robot>> orderFromGame = game.getRespawnFieldOrder();

    /* Add the robots, that die (while the player is still alive) to the respawn queue.
     * The queue is in the order, that the players receive the messages to respawn
     * in a parallel process. */
    orderFromGame.forEach(
        (field, robots) ->
            robots.stream()
                .filter(robot -> !robot.isAlive())
                .filter(robot -> robot.getPosition() != null)
                .forEach(
                    robot -> {
                      if (queue.get(field) != null) {
                        Deque<Robot> i = queue.get(field);
                        i.offer(robot);
                      } else {
                        queue.put(field, new ArrayDeque<>(List.of(robot)));
                      }
                    }));

    game.setRobotOnRoundRespawnQueue(queue);

    game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .filter(robot -> !robot.isAlive())
        .forEach(
            robot -> {
              // VARIANT III
              if (game.getLobby().getOptions().isWeakDuplicatedActive()) {
                weakRobotDuplicate(robot);
              } else {
                robot.alive();
              }
            });
    game.getPlayers()
        .forEach(
            player -> {
              Robot robot = player.getRobot();
              Robots robotType = robot.getType();
              FloorField position = robot.getPosition();
              FloorField respawnPosition = robot.getRespawnField();
              int health = robot.getLives();
              Direction direction = robot.getDirection();
              int receivedDamage = robot.getDamage();
              int latestCheckpoint = robot.getCurrentCheckpoint();

              RobotInformation robotInformation =
                  new RobotInformation(
                      health,
                      position,
                      direction,
                      receivedDamage,
                      latestCheckpoint,
                      respawnPosition);

              if (position != null) {
                step.addRobotInformation(robotType, robotInformation);
              }
            });
  }

  protected void checkWinnerCheckpoints(Robot robot) {
    if (robot.getCurrentCheckpoint() == game.getFloorPlan().getCheckpoints().size()) {
      tempParallelAnimation.addAnimation(
          new SingleAnimation(
              AnimationType.WIN,
              robot.getType(),
              robot.getPosition().getX(),
              robot.getPosition().getY(),
              robot.getDirection()));
      endGame(game.getPlayer(robot));
    }
  }

  private void checkWinnerDeath() {
    List<Player> alive =
        game.getPlayers().stream().filter(Player::isNotSpectator).collect(Collectors.toList());
    if (alive.size() == 1 && !game.isHasStartWithCommand()) {
      endGame(alive.get(0));
    } else if (game.isHasStartWithCommand() && alive.isEmpty()) {
      endGame(
          new Player(
              PlayerType.HUMAN_PLAYER,
              null,
              new UserDTO(null, "SYSTEM", "NOPASSWORD", "system@.de", new UserData(2))));
    } else if (alive.isEmpty()) {
      LOG.error("All Robots are dead, but the game has not terminated.");
    }
  }

  @SuppressWarnings("BusyWait")
  private void endGame(Player player) {
    game.getPlayers()
        .forEach(
            p -> {
              Robot robot = p.getRobot();
              Robots robotType = robot.getType();
              FloorField position = robot.getPosition();
              FloorField respawnPosition = robot.getRespawnField();
              int health = robot.getLives();
              Direction direction = robot.getDirection();
              int receivedDamage = robot.getDamage();
              int latestCheckpoint = robot.getCurrentCheckpoint();

              RobotInformation robotInformation =
                  new RobotInformation(
                      health,
                      position,
                      direction,
                      receivedDamage,
                      latestCheckpoint,
                      respawnPosition);

              if (position != null) {
                step.addRobotInformation(robotType, robotInformation);
              }
            });
    ServerInternalMessage msg = new GameLogicFinish(step, game.getLobby());
    bus.post(msg);

    while (game.isInGuiShowPhase()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    if (!game.isHasStartWithCommand()) {
      createUserStatistics();
    }
    game.setWinner(player);
    ServerMessage serverMessage = new WinMessage(game, player);
    String id = game.getGameId();
    gameService.sendToAllInGame(id, serverMessage);
    game.getLobby().setLobbyStatus(LobbyStatus.WAITING);
    game.getLobby().clearRobotSelection();
    game.getPlayers().forEach(p -> game.getLobby().setReady(p.getUser()));
    gameService.getGameManagement().dropGame(id);
    gameService.getGameTimerService().dropAllTimer(id);
    gameService.getGameLogicService().stopLogic(id);
  }

  private void createUserStatistics() {
    List<Robot> robotList =
        game.getPlayers().stream()
            .filter(Player::isNotSpectator)
            .map(Player::getRobot)
            .sorted(Comparator.comparing(Robot::getCurrentCheckpoint).reversed())
            .collect(Collectors.toList());

    int placement = 1;
    int prevCheckpoint = robotList.get(0).getCurrentCheckpoint();

    for (Robot robot : robotList) {
      if (prevCheckpoint > robot.getCurrentCheckpoint()) {
        placement++;
      }

      UserStatistic userStatistic = createUserStatistic(placement);
      bus.post(new UpdateUserStatsMessage(game.getPlayer(robot).getUser(), userStatistic));
    }
  }

  private UserStatistic createUserStatistic(int placement) {
    return new UserStatistic(placement, LocalDateTime.now());
  }

  /**
   * This method corresponds to IV
   *
   * <p>In this method the robot are repaired.
   */
  private void repair() {
    repair(false);
  }

  /**
   * Repairmethode only for command issues.
   *
   * @param isCommandMode is from command.
   */
  public void repair(boolean isCommandMode) {
    game.getPlayers().stream()
        .map(Player::getRobot)
        .filter(Robot::isAlive)
        .forEach(
            r -> {
              try {
                Repair repairField;
                if (!isCommandMode) {
                  repairField =
                      (Repair) r.getPosition().getBasicFloorField().getOperations().get(6);
                } else {
                  repairField = new Repair(1);
                }
                int slots = r.repair(repairField.getNumber());
                if (slots > 0) {
                  gameService.sendUnblockCardSlotInteraction(
                      game.getPlayer(r).getUser(), game.getGameId(), slots);
                }
              } catch (NullPointerException e) {
                // FloorField has no repair field
              }
              if (r.getPosition().getBasicFloorField().getOperations().containsKey(7)
                  && (r.repair(1) > 0)) {
                gameService.sendUnblockCardSlotInteraction(
                    game.getPlayer(r).getUser(), game.getGameId(), 1);
              }
            });
  }

  /**
   * Corresponds to Variant II of the game in the german instruction handles the shutdown of robots.
   */
  public void shutdown() {
    game.getPlayers().stream()
        .map(Player::getRobot)
        .filter(Robot::isShutdown)
        .forEach(Robot::turnOn);
    game.getPlayers().stream()
        .map(Player::getRobot)
        .filter(Robot::isNeedToShutdown)
        .forEach(
            robot -> {
              robot.shutdown();
              robot.setIsNeedToShutdown(false);
            });
  }

  private void damage(Robot robot) {
    if (robot.damage()) {
      if (robot.getLives() > 0) {
        gameService.sendChangeSpectatorMode(game, robot);
      }
      gameService.sendRobotDeadChatMessage(game.getGameId(), game.getPlayer(robot));
      checkWinnerDeath();
    }
  }

  private void destroy(Robot robot) {
    if (robot.destroy()) {
      gameService.sendChangeSpectatorMode(game, robot);
    }
    gameService.sendRobotDeadChatMessage(game.getGameId(), game.getPlayer(robot));
    checkWinnerDeath();
  }
}
