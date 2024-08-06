package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.animation.Animation;
import de.uol.swp.common.game.animation.AnimationType;
import de.uol.swp.common.game.animation.ParallelAnimation;
import de.uol.swp.common.game.animation.SeparationPoint;
import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.floor.operation.Checkpoint;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The GameLogicTest class represents a unit test for the GameLogic class, which is responsible for
 *
 * <p>processing the game logic of a game.
 */
@SuppressWarnings("UnstableApiUsage")
class GameLogicTest {

  EventBus bus = new EventBus();
  AuthenticationService authenticationService =
      new AuthenticationService(bus, new UserManagement(new MainMemoryBasedUserStore()));
  User user = new UserDTO(UUID.randomUUID().toString(), "test", "test", "test", new UserData(0));
  User user1 =
      new UserDTO(UUID.randomUUID().toString(), "test1", "test1", "test1", new UserData(0));
  User user2 =
      new UserDTO(UUID.randomUUID().toString(), "test2", "test1", "test1", new UserData(0));

  LobbyManagement lobbyManagement;
  LobbyService lobbyService;

  GameService gameService;
  GameManagement gameManagement;
  Game game;
  Game game3P;
  GameLogic logic;
  FloorPlan floorPlan =
      new FloorPlan(
          "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "201;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "097;000;000;301;000;000;000;000;000;000;000;000\n"
              + "097;000;000;301;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000",
          true);

  FloorPlan floorPlan3P =
      new FloorPlan(
          "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "201;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "097;000;000;301;000;000;000;000;000;000;000;000\n"
              + "097;000;000;301;000;000;000;000;000;000;000;000\n"
              + "097;000;000;000;000;000;000;000;000;000;000;000",
          true);

  FloorField start;
  FloorField start1;

  private static void assertRobotPosition(Robot robot, Direction dir, FloorField position) {
    assertEquals(dir, robot.getDirection());
    assertEquals(position, robot.getPosition());
  }

  private static void assertRobotPosition(Robot robot, Direction dir, int xPos, int yPos) {
    assertEquals(dir, robot.getDirection());
    assertEquals(xPos, robot.getPosition().getX());
    assertEquals(yPos, robot.getPosition().getY());
  }

  @BeforeEach
  void setup() throws NoSuchElementException {
    Lobby lobby = new LobbyDTO("test", user);
    lobby.setRobotSelected(Robots.BOB, user);
    lobby.joinUser(user1);
    lobby.setRobotSelected(Robots.DUSTY, user1);
    game = new GameDTO(lobby, floorPlan);
    lobbyManagement = new LobbyManagement();
    lobbyService = new LobbyService(lobbyManagement, authenticationService, bus);
    gameManagement = new GameManagement();
    gameService = new GameService(bus, gameManagement, lobbyManagement, lobbyService);
    start = game.getPlayer(user).getRobot().getPosition();
    start1 = game.getPlayer(user1).getRobot().getPosition();

    Lobby lobby3P = new LobbyDTO("Lobby3P", user);
    lobby3P.setRobotSelected(Robots.BOB, user);
    lobby3P.joinUser(user1);
    lobby3P.setRobotSelected(Robots.DUSTY, user1);
    lobby3P.joinUser(user2);
    lobby3P.setRobotSelected(Robots.GANDALF, user2);
    game3P = new GameDTO(lobby3P, floorPlan3P);
  }

  @AfterEach
  void teardown() {
    try {
      lobbyManagement.dropLobby("test");
      lobbyManagement.dropLobby("ASDF");
      lobbyManagement.dropLobby("CLEAN");
      lobbyManagement.dropLobby("Lobby3P");
    } catch (IllegalArgumentException e) {
      // empty
    }
  }

  /** Shows that multiple steps are gone with one card and that the robot stops at walls. */
  @Test
  void move3HitWallAfter2StepsTest() {
    ProgrammingCard card = new ProgrammingCard(10, CardType.MOVE3);
    card.setOwner(game.getPlayer(user).getRobot().getType());
    game.getPlayer(user).getRobot().setSelectedCards(new ProgrammingCard[] {card});
    logic = createGameLogic(game);
    assertEquals(logic.getGame().getPlayer(user).getRobot().getPosition(), start);
    logic.run();
    assertEquals(
        logic.getGame().getPlayer(user).getRobot().getPosition(),
        start.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST));
  }

  @Test
  void pusherTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;064;000;000;000;000;000;000\n"
                + "333;000;000;000;133;033;000;000;000;000;000;000\n"
                + "000;000;000;033;097;233;000;000;000;000;000;000\n"
                + "000;000;000;233;333;133;301;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.joinUser(user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.DUSTY, user);
    lobby.setRobotSelected(Robots.GREGOR, user2);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    Robot testRobot2 = game.getPlayer(user2).getRobot();
    testRobot2.setPosition(
        testRobot
            .getPosition()
            .getNeighbour(Direction.NORTH)
            .getNeighbour(Direction.NORTH)
            .getNeighbour(Direction.NORTH)
            .getNeighbour(Direction.NORTH));

    // Push to East

    FloorField startField = testRobot.getPosition();
    testRobot.setPosition(startField.getNeighbour(Direction.NORTH));
    FloorField pusherField = testRobot.getPosition();
    FloorField pushedToField = pusherField.getNeighbour(Direction.EAST);

    gameLogic.pusher(1);

    assertNotEquals(pusherField, testRobot.getPosition());
    assertEquals(pushedToField, testRobot.getPosition());

    // Push to North in Pit

    gameLogic.pusher(1);
    assertEquals(startField, testRobot.getPosition());
    setInt(testRobot, 3);

    // Push to South

    testRobot.setPosition(startField);
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.SOUTH));
    pusherField = testRobot.getPosition();
    gameLogic.pusher(1);

    assertNotEquals(pusherField, testRobot.getPosition());
    assertEquals(pusherField, testRobot.getPosition().getNeighbour(Direction.EAST));

    // Push to Robot
    pusherField = testRobot.getPosition();
    testRobot2.setPosition(pusherField.getNeighbour(Direction.SOUTH));
    gameLogic.pusher(1);

    assertEquals(pusherField.getNeighbour(Direction.SOUTH), testRobot.getPosition());
    assertEquals(
        pusherField.getNeighbour(Direction.SOUTH).getNeighbour(Direction.SOUTH),
        testRobot2.getPosition());

    // Push to West

    testRobot.setPosition(startField);
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.WEST));
    pusherField = testRobot.getPosition();
    gameLogic.pusher(1);

    assertNotEquals(pusherField, testRobot.getPosition());
    assertEquals(pusherField, testRobot.getPosition().getNeighbour(Direction.SOUTH));

    // Push to East

    testRobot.setPosition(startField);
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    pusherField = testRobot.getPosition();
    gameLogic.pusher(1);

    assertNotEquals(pusherField, testRobot.getPosition());
    assertEquals(pusherField, testRobot.getPosition().getNeighbour(Direction.NORTH));

    pusherField = testRobot.getPosition();
    // Push to East in Wall
    gameLogic.pusher(1);
    assertEquals(pusherField, testRobot.getPosition());

    // Push to out of Plan
    testRobot.setPosition(startField);
    testRobot.setPosition(
        testRobot
            .getPosition()
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.WEST)
            .getNeighbour(Direction.NORTH));
    gameLogic.pusher(1);

    assertEquals(startField, testRobot.getPosition());
  }

  @Test
  void laserTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;150;000;000;000;000;000;000;000\n"
                + "000;000;000;000;156;000;000;000;000;000;000;000\n"
                + "000;000;148;053;053;053;348;000;000;000;000;000\n"
                + "000;000;000;000;097;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();

    // test single laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(2, getInt(testRobot, "damage")); // V1: 0; V2: 2

    // test double laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(4, getInt(testRobot, "damage")); // V1: 2; V2: 4

    // test triple laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(7, getInt(testRobot, "damage")); // V1: 5; V2: 7

    FloorPlan floorPlan2 =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;150;000;000;000;000;000;000;000\n"
                + "000;000;000;000;156;000;000;000;000;000;000;000\n"
                + "000;000;155;153;153;153;348;000;000;000;000;000\n"
                + "000;000;000;097;097;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(2));
    Lobby lobby2 = new LobbyDTO("CLEAN", user);
    lobby2.joinUser(user2);
    lobby2.setRobotSelected(Robots.GANDALF, user);
    lobby2.setRobotSelected(Robots.BOB, user2);

    Game game2 = new GameDTO(lobby2, floorPlan2);
    GameLogic gameLogic2 = createGameLogic(game2);
    Robot testRobot2 = game2.getPlayer(user).getRobot();
    Robot testRobot3 = game2.getPlayer(user2).getRobot();

    // test Robot in front of Robot
    testRobot2.setPosition(testRobot2.getPosition().getNeighbour(Direction.NORTH));
    testRobot3.setPosition(testRobot3.getPosition().getNeighbour(Direction.NORTH));
    gameLogic2.laser();
    assertEquals(0, getInt(testRobot2, "damage"));
    assertEquals(1, getInt(testRobot3, "damage"));
  }

  @Test
  void laserTest2() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;156;000;000;000;000;000;000;000\n"
                + "000;000;000;000;053;000;000;000;000;000;000;000\n"
                + "000;000;000;000;097;000;000;000;000;000;000;000\n"
                + "000;000;000;000;001;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();

    // no move
    gameLogic.laser();
    assertEquals(0, getInt(testRobot, "damage"));

    // single laser beam but no source
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(0, getInt(testRobot, "damage"));

    // double laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(2, getInt(testRobot, "damage"));
  }

  @Test
  void laserTest3() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;156;000;000;000;000;000;000;000\n"
                + "000;000;000;097;053;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();

    // test single laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    gameLogic.laser();
    assertEquals(0, getInt(testRobot, "damage"));

    // test double laser
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.laser();
    assertEquals(2, getInt(testRobot, "damage"));
  }

  @Test
  void laserTest4() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;201;000;000;000;000;000;000;000\n"
                + "000;000;000;101;072;348;000;000;000;000;000;000\n"
                + "000;000;000;097;053;000;000;000;000;000;000;000\n"
                + "000;000;000;000;001;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();

    // laser cross, but no laser source in robots direction
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    gameLogic.laser();
    assertEquals(0, getInt(testRobot, "damage"));
  }

  @Test
  void weakRobotDuplicate() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;064;097;064;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);
    lobby.getOptions().setWeakDuplicatedActive(true);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    testRobot.setSelectedCards(
        new ProgrammingCard[] {
          new ProgrammingCard(200, CardType.MOVE1), new ProgrammingCard(210, CardType.MOVE1)
        });
    testRobot.getSelectedCards()[0].setOwner(testRobot.getType());
    testRobot.getSelectedCards()[1].setOwner(testRobot.getType());

    assertEquals(0, testRobot.getDamage());
    assertEquals(3, testRobot.getLives());

    gameLogic.run();

    assertEquals(2, testRobot.getLives());
    assertEquals(2, testRobot.getDamage());
  }

  @Test
  void weakRobotDuplicate_NotActive() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;064;097;064;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);
    lobby.getOptions().setWeakDuplicatedActive(false);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    testRobot.setSelectedCards(
        new ProgrammingCard[] {
          new ProgrammingCard(200, CardType.MOVE1), new ProgrammingCard(210, CardType.MOVE1)
        });
    testRobot.getSelectedCards()[0].setOwner(testRobot.getType());
    testRobot.getSelectedCards()[1].setOwner(testRobot.getType());

    assertEquals(0, testRobot.getDamage());
    assertEquals(3, testRobot.getLives());

    gameLogic.run();

    assertEquals(2, testRobot.getLives());
    assertEquals(0, testRobot.getDamage());
  }

  @Test
  void gearsTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;097;038;000;000;000;000;000;000\n"
                + "000;000;000;000;000;137;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    // create two test user and lobby
    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user1 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.joinUser(user1);
    lobby.setRobotSelected(Robots.BOB, user);
    lobby.setRobotSelected(Robots.DUSTY, user1);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    Robot testRobot1 = game.getPlayer(user1).getRobot();
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    testRobot1.setPosition(testRobot.getPosition().getNeighbour(Direction.SOUTH));

    // test clockwise rotation gear
    assertEquals(Direction.EAST, testRobot.getDirection());
    assertEquals(Direction.EAST, testRobot1.getDirection());
    gameLogic.gears();
    assertEquals("38", testRobot.getPosition().getBasicFloorField().getId());
    assertEquals(Direction.SOUTH, testRobot.getDirection());

    // test counterclockwise rotation gear and rotated gear field with 1GL
    assertEquals(Direction.NORTH, testRobot1.getDirection());
    gameLogic.gears();
    assertEquals("37", testRobot1.getPosition().getBasicFloorField().getId());
    assertEquals(Direction.WEST, testRobot1.getDirection());

    // test for multiple roboters to check if everyone gets rotated
    gameLogic.gears();
    assertEquals(Direction.NORTH, testRobot.getDirection());
    assertEquals(Direction.SOUTH, testRobot1.getDirection());
  }

  int getInt(Robot robot, String value) {

    // Get robot field value
    Field roboField;
    try {
      roboField = robot.getClass().getDeclaredField(value);
      roboField.setAccessible(true);

      return roboField.getInt(robot);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("SameParameterValue")
  void setInt(Robot robot, int newValue) {

    // Reset robot field value
    Field roboField;
    try {
      roboField = robot.getClass().getDeclaredField("lives");
      roboField.setAccessible(true);

      roboField.setInt(robot, newValue);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /** Shows that turning works and the cards of one player are executed in the right order. */
  @Test
  void moveBackAndTurn180Test() throws NoSuchElementException {
    ProgrammingCard[] card =
        new ProgrammingCard[] {
          new ProgrammingCard(10, CardType.U_TURN), new ProgrammingCard(5, CardType.BACKUP)
        };
    Arrays.stream(card)
        .forEach(
            c -> {
              try {
                c.setOwner(game.getPlayer(user).getRobot().getType());
              } catch (NoSuchElementException e) {
                throw new RuntimeException(e);
              }
            });
    game.getPlayer(user).getRobot().setSelectedCards(card);
    logic = createGameLogic(game);
    assertEquals(logic.getGame().getPlayer(user).getRobot().getPosition(), start);
    logic.run();
    assertEquals(
        logic.getGame().getPlayer(user).getRobot().getPosition(),
        start.getNeighbour(Direction.EAST));
  }

  @Test
  void push2HitWallAfter1StepTest() throws NoSuchElementException {
    ProgrammingCard[] card =
        new ProgrammingCard[] {
          new ProgrammingCard(10, CardType.MOVE1), new ProgrammingCard(20, CardType.TURN_LEFT)
        };
    Arrays.stream(card)
        .forEach(
            c -> {
              try {
                c.setOwner(game.getPlayer(user).getRobot().getType());
              } catch (NoSuchElementException e) {
                throw new RuntimeException(e);
              }
            });
    game.getPlayer(user).getRobot().setSelectedCards(card);

    ProgrammingCard[] card1 =
        new ProgrammingCard[] {
          new ProgrammingCard(20, CardType.TURN_LEFT),
          new ProgrammingCard(5, CardType.MOVE1),
          new ProgrammingCard(20, CardType.TURN_RIGHT),
          new ProgrammingCard(8, CardType.MOVE2)
        };
    Arrays.stream(card1)
        .forEach(
            c -> {
              try {
                c.setOwner(game.getPlayer(user1).getRobot().getType());
              } catch (NoSuchElementException e) {
                throw new RuntimeException(e);
              }
            });
    game.getPlayer(user1).getRobot().setSelectedCards(card1);

    logic = createGameLogic(game);

    Robot testRobot = logic.getGame().getPlayer(user).getRobot();
    Robot testRobot1 = logic.getGame().getPlayer(user1).getRobot();

    assertRobotPosition(testRobot, Direction.EAST, start);
    assertRobotPosition(testRobot1, Direction.EAST, start1);

    logic.run();

    assertRobotPosition(
        testRobot,
        Direction.NORTH,
        start.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST));
    assertRobotPosition(
        testRobot1,
        Direction.EAST,
        start1.getNeighbour(Direction.NORTH).getNeighbour(Direction.EAST));
  }

  @Test
  void movePitTest() throws NoSuchElementException {
    ProgrammingCard[] card =
        new ProgrammingCard[] {
          new ProgrammingCard(20, CardType.BACKUP), new ProgrammingCard(10, CardType.MOVE3)
        };
    Arrays.stream(card)
        .forEach(
            c -> {
              try {
                c.setOwner(game.getPlayer(user).getRobot().getType());
              } catch (NoSuchElementException e) {
                throw new RuntimeException(e);
              }
            });
    game.getPlayer(user).getRobot().setHandCards(card);

    logic = createGameLogic(game);

    assertEquals(start, logic.getGame().getPlayer(user).getRobot().getPosition());

    logic.run();

    assertEquals(start, logic.getGame().getPlayer(user).getRobot().getPosition());
  }

  @Test
  void checkpointTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;000;033;000;000;000;000;000;000;000\n"
                + "000;000;000;000;033;000;000;000;000;000;000;000\n"
                + "000;064;333;333;001;133;133;064;133;000;000;000\n"
                + "000;000;000;000;233;000;000;000;000;000;000;000\n"
                + "000;000;000;000;233;000;000;000;000;000;000;000\n"
                + "097;0002;0001;000;064;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot robot = game.getPlayer(user).getRobot();
    FloorField start = robot.getPosition();
    assertEquals(
        2,
        ((Checkpoint)
                start.getNeighbour(Direction.EAST).getBasicFloorField().getOperations().get(7))
            .getNumber());
    assertEquals(0, robot.getCurrentCheckpoint());

    gameLogic.processingCards(CardType.MOVE1, robot);
    assertEquals(start.getNeighbour(Direction.EAST), robot.getPosition());
    assertEquals(0, robot.getCurrentCheckpoint());

    gameLogic.processingCards(CardType.MOVE1, robot);
    assertEquals(1, robot.getCurrentCheckpoint());

    gameLogic.processingCards(CardType.BACKUP, robot);
    assertEquals(2, robot.getCurrentCheckpoint());
  }

  @Test
  void testPresses() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;039;000;000;000;000;000;000\n"
                + "000;000;000;000;000;014;000;000;000;000;000;000\n"
                + "000;000;000;000;000;097;000;000;000;000;000;000\n"
                + "000;000;000;000;000;214;000;000;000;000;000;000\n"
                + "000;000;000;000;000;239;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    FloorField startField = testRobot.getPosition();
    assertEquals(startField, testRobot.getPosition());

    testRobot.setDirection(Direction.NORTH);
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    assertNotEquals(startField, testRobot.getPosition());
    gameLogic.run();
    assertEquals(startField, testRobot.getPosition());
    assertEquals(2, getInt(testRobot, "lives"));

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));

    gameManagement.addGame(game);
    gameLogic.run();
    assertEquals(startField, testRobot.getPosition());
    assertEquals(1, getInt(testRobot, "lives"));

    assertEquals(startField, testRobot.getPosition());
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.SOUTH));
    assertEquals(startField.getNeighbour(Direction.SOUTH), testRobot.getPosition());

    gameLogic.run();
    assertEquals(0, getInt(testRobot, "lives"));

    Robot testRobot2 = new Robot(Robots.BOB, startField);
    assertEquals(3, testRobot2.getLives());

    testRobot2.setPosition(testRobot2.getPosition().getNeighbour(Direction.WEST));
    assertEquals(startField.getNeighbour(Direction.WEST), testRobot2.getPosition());
    gameLogic.run();
    assertEquals(3, getInt(testRobot2, "lives"));
  }

  @Test
  void beltsTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;214;000;000;000\n"
                + "000;013;113;000;312;012;000;114;214;000;000;000\n"
                + "000;313;213;000;212;112;000;000;214;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;105;105;000;205;000;000;000;000\n"
                + "114;000;214;000;000;000;000;205;000;000;000;000\n"
                + "000;000;000;000;005;000;000;000;000;000;000;000\n"
                + "014;000;314;000;005;000;305;305;000;000;000;000\n"
                + "097;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    FloorField cursor = testRobot.getPosition(); // (0,0)
    FloorField start = cursor;

    // Put robot on belt
    testRobot.setPosition(cursor.getNeighbour(Direction.NORTH));

    // Move to north
    cursor = cursor.getNeighbour(Direction.NORTH).getNeighbour(Direction.NORTH); // (0,2)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Move to east
    testRobot.setPosition(cursor.getNeighbour(Direction.NORTH));
    cursor = cursor.getNeighbour(Direction.NORTH).getNeighbour(Direction.EAST); // (1,3)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertEquals(cursor, testRobot.getPosition());

    // Move to south
    testRobot.setPosition((cursor.getNeighbour(Direction.EAST)));
    cursor = cursor.getNeighbour(Direction.EAST).getNeighbour(Direction.SOUTH); // (2,2)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Move to west
    testRobot.setPosition((cursor.getNeighbour(Direction.SOUTH)));
    cursor = cursor.getNeighbour((Direction.SOUTH)).getNeighbour(Direction.WEST); // (1,1)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Move the cursor over
    cursor =
        cursor
            .getNeighbour(Direction.EAST)
            .getNeighbour(Direction.EAST)
            .getNeighbour(Direction.EAST); // (4,1)

    cursor =
        start
            .getNeighbour(Direction.NORTH)
            .getNeighbour(Direction.EAST)
            .getNeighbour(Direction.EAST)
            .getNeighbour(Direction.EAST)
            .getNeighbour(Direction.EAST);

    // Expressmove to north
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.NORTH).getNeighbour(Direction.NORTH); // (4,3)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Expressmove to east
    cursor = cursor.getNeighbour(Direction.NORTH); // (4,4)
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST); // (6,4)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Expressmove to south
    cursor = cursor.getNeighbour(Direction.EAST); // (7,4)
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.SOUTH).getNeighbour(Direction.SOUTH); // (7,2)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Expressmove to west
    cursor = cursor.getNeighbour(Direction.SOUTH); // (7,1)
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.WEST).getNeighbour(Direction.WEST); // (5,1)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // move cursor
    for (int i = 0; i < 5; i++) {
      cursor = cursor.getNeighbour(Direction.NORTH); // (5,6)
    }

    // Counterclockwise Curve north
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.NORTH); // (5,7)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.NORTH, cursor);

    // Counterclockwise Curve west
    cursor = cursor.getNeighbour(Direction.WEST); // (4,7)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.WEST, cursor);

    // Counterclockwise Curve south
    cursor = cursor.getNeighbour(Direction.SOUTH); // (4,6)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.SOUTH, cursor);

    // Counterclockwise Curve east
    cursor = cursor.getNeighbour(Direction.EAST); // (5,6)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // move cursor
    for (int i = 0; i < 4; i++) {
      cursor = cursor.getNeighbour(Direction.WEST); // (1,6)
    }
    // Clockwise Curve north
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.NORTH); // (1,7)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.SOUTH, cursor);

    // Clockwise Curve east
    cursor = cursor.getNeighbour(Direction.EAST); // (2,7)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.WEST, cursor);

    // Clockwise Curve south
    cursor = cursor.getNeighbour(Direction.SOUTH); // (2,6)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.NORTH, cursor);

    // Clockwise Curve west
    cursor = cursor.getNeighbour(Direction.WEST); // (1,6)
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // move Cursor
    for (int i = 0; i < 2; i++) {
      cursor = cursor.getNeighbour(Direction.NORTH); // (1,8)
    }
    for (int i = 0; i < 7; i++) {
      cursor = cursor.getNeighbour(Direction.EAST); // (8,8)
    }

    // Interchange without turning
    testRobot.setPosition(cursor);
    cursor = cursor.getNeighbour(Direction.SOUTH); // 8,7
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.EAST, cursor);

    // Interchange with Turning
    testRobot.setPosition(cursor.getNeighbour(Direction.WEST));
    gameLogic.expressOne();
    gameLogic.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot, Direction.SOUTH, cursor);

    // PattSituation
    FloorPlan floorPlan2 =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;214;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;005;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "114;000;314;000;114;000;314;000;000;000;000;000\n"
                + "097;000;000;000;000;014;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user1 = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(1));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(2));
    User user3 = new UserDTO(UUID.randomUUID().toString(), "c", "c", "c", new UserData(3));
    Lobby lobby2 = new LobbyDTO("CLEAN", user1);
    lobby2.joinUser(user2);
    lobby2.joinUser(user3);
    lobby2.setRobotSelected(Robots.BOB, user1);
    lobby2.setRobotSelected(Robots.DUSTY, user2);
    lobby2.setRobotSelected(Robots.GANDALF, user3);

    Game game2 = new GameDTO(lobby2, floorPlan2);
    GameLogic gameLogic2 = createGameLogic(game2);
    Robot testRobot1 = game2.getPlayer(user1).getRobot();
    Robot testRobot2 = game2.getPlayer(user2).getRobot();
    Robot testRobot3 = game2.getPlayer(user3).getRobot();
    FloorField cursor1 = testRobot1.getPosition(); // (0,0)
    FloorField cursor2;
    FloorField cursor3;
    testRobot2.setPosition(cursor1);
    testRobot3.setPosition(cursor1);

    // simple pattSituation
    cursor1 = cursor1.getNeighbour(Direction.NORTH); // (0,1)
    cursor2 = cursor1.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST); // (2,1)
    testRobot1.setPosition(cursor1);
    testRobot2.setPosition(cursor2);
    gameLogic2.expressOne();
    gameLogic2.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot1, Direction.EAST, cursor1);
    assertRobotPosition(testRobot2, Direction.EAST, cursor2);

    // threeway pattSituation
    cursor2 = cursor2.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST); // (4,1)
    cursor1 = cursor2.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST); // (6,1)
    cursor3 = cursor2.getNeighbour(Direction.SOUTH).getNeighbour(Direction.EAST); // (5,0)
    testRobot1.setPosition(cursor1);
    testRobot2.setPosition(cursor2);
    testRobot3.setPosition(cursor3);
    gameLogic2.expressOne();
    gameLogic2.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot1, Direction.EAST, cursor1);
    assertRobotPosition(testRobot2, Direction.EAST, cursor2);
    assertRobotPosition(testRobot3, Direction.EAST, cursor3);

    // Express-belt - NormalBelt pseudo Patt
    for (int i = 0; i < 4; i++) {
      cursor3 = cursor3.getNeighbour(Direction.NORTH); // (5,4)
    }
    cursor2 = cursor3.getNeighbour(Direction.NORTH).getNeighbour(Direction.NORTH); // (5,6)
    testRobot2.setPosition(cursor2);
    testRobot3.setPosition(cursor3);
    gameLogic2.expressOne();
    gameLogic2.expressTwoAndNormalBelts();
    assertRobotPosition(testRobot2, Direction.EAST, cursor2);
    assertRobotPosition(testRobot3, Direction.EAST, cursor3.getNeighbour(Direction.NORTH));
  }

  @Test
  void robotLaserTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;097;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user1 = new UserDTO(UUID.randomUUID().toString(), "b", "b", "b", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "c", "c", "c", new UserData(5));
    User user3 = new UserDTO(UUID.randomUUID().toString(), "d", "d", "d", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.joinUser(user1);
    lobby.joinUser(user2);
    lobby.joinUser(user3);
    lobby.setRobotSelected(Robots.DUSTY, user);
    lobby.setRobotSelected(Robots.BOB, user1);
    lobby.setRobotSelected(Robots.GANDALF, user2);
    lobby.setRobotSelected(Robots.GREGOR, user3);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);

    // Get all robots from game
    Robot testRobot = game.getPlayer(user).getRobot();
    Robot testRobot1 = game.getPlayer(user1).getRobot();
    Robot testRobot2 = game.getPlayer(user2).getRobot();
    Robot testRobot3 = game.getPlayer(user3).getRobot();

    FloorField startField = testRobot.getPosition();
    testRobot1.setPosition(startField.getNeighbour(Direction.EAST).getNeighbour(Direction.EAST));
    testRobot1.setDirection(Direction.NORTH);
    testRobot2.setPosition(
        testRobot1.getPosition().getNeighbour(Direction.EAST).getNeighbour(Direction.EAST));
    testRobot3.setPosition(testRobot1.getPosition().getNeighbour(Direction.NORTH));
    gameLogic.robotLaser();

    /*
     * Positions Robot on Plan
     * ## ## r3 ## ##
     * ## ## ## ## ##
     * r0 ## r1 ## r2
     * ## ## ## ## ##
     */

    // Test RobotLaser blocked by robot (r0 shoots and hits r1 but not r2)
    assertEquals(0, getPrivate(testRobot));
    assertEquals(1, getPrivate(testRobot1));
    assertEquals(0, getPrivate(testRobot2));
    assertEquals(1, getPrivate(testRobot3));
  }

  @Test
  void repairTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;000;017;000;000;000;000;000;000;000\n"
                + "000;000;000;048;097;067;000;000;000;000;000;000\n"
                + "000;000;000;000;0661;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    FloorField startField = testRobot.getPosition();
    assertEquals(startField, testRobot.getPosition());

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    assertNotEquals(startField, testRobot.getPosition());

    gameLogic.run();
    assertEquals(startField, testRobot.getPosition());
    assertEquals(2, getInt(testRobot, "lives"));

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.WEST));
    assertEquals(startField.getNeighbour(Direction.WEST), testRobot.getPosition());
    gameLogic.laser();
    assertEquals(1, getInt(testRobot, "damage"));
    gameLogic.laser();
    assertEquals(2, getInt(testRobot, "damage"));

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.SOUTH));
    assertEquals(startField.getNeighbour(Direction.SOUTH), testRobot.getPosition());

    // test repair field 66 plus checkpoint on repair field
    gameLogic.run();
    assertEquals(0, getInt(testRobot, "damage"));
    assertNotEquals(1, getInt(testRobot, "damage"));

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.NORTH));
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.WEST));
    assertEquals(startField.getNeighbour(Direction.WEST), testRobot.getPosition());
    gameLogic.laser();
    assertEquals(1, getInt(testRobot, "damage"));
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.EAST));
    assertEquals(startField.getNeighbour(Direction.EAST), testRobot.getPosition());
    assertEquals(1, getInt(testRobot, "damage"));

    // test repair field 67
    gameLogic.run();
    assertEquals(0, getInt(testRobot, "damage"));
  }

  @Test
  void checkWinningTest() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;064;000;000;000;000;000;000;000\n"
                + "000;000;000;000;017;000;000;000;000;000;000;000\n"
                + "000;000;000;048;097;067;000;000;000;000;000;000\n"
                + "000;000;000;000;132;0001;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.DUSTY, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot testRobot = game.getPlayer(user).getRobot();
    FloorField startField = testRobot.getPosition();
    assertEquals(startField, testRobot.getPosition());

    gameLogic.run();
    assertNotEquals(testRobot.getCurrentCheckpoint(), game.getFloorPlan().getCheckpoints().size());
    assertEquals(startField, testRobot.getPosition());

    testRobot.setPosition(testRobot.getPosition().getNeighbour(Direction.SOUTH));
    assertEquals(startField.getNeighbour(Direction.SOUTH), testRobot.getPosition());

    gameLogic.run();
    assertEquals(
        startField.getNeighbour(Direction.SOUTH).getNeighbour(Direction.EAST),
        testRobot.getPosition());
    assertEquals(testRobot.getCurrentCheckpoint(), game.getFloorPlan().getCheckpoints().size());
  }

  int getPrivate(Robot robot) {
    // Reset Roboter lives
    Field getLives;
    try {
      getLives = robot.getClass().getDeclaredField("damage");
      getLives.setAccessible(true);

      return getLives.getInt(robot);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void stepObjectTestBelt() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;004;105;064;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;0002\n"
                + "197;105;105;000;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.setRobotSelected(Robots.OCEAN, user);

    Game game = new GameDTO(lobby, floorPlan);
    gameManagement.addGame(game);
    GameLogic gameLogic = createGameLogic(game);
    Robot robotOcean = game.getPlayer(user).getRobot();
    List<Animation> actualStep;
    ParallelAnimation parallelAnimationStep;
    Animation subSteps;

    assertRobotPosition(robotOcean, Direction.EAST, 0, 11);
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotOcean, Direction.EAST, 1, 11);

    gameLogic.expressOne();
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(0);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertRobotPosition(robotOcean, Direction.EAST, 2, 11);
    assertEquals(AnimationType.MOVED_EAST_BY_BELT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());

    gameLogic.expressTwoAndNormalBelts();
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(1);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertRobotPosition(robotOcean, Direction.EAST, 3, 11);
    assertEquals(AnimationType.MOVED_EAST_BY_BELT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());

    // move Robot Two Up
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.NORTH));
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.NORTH));
    assertRobotPosition(robotOcean, Direction.EAST, 3, 9);

    gameLogic.expressOne();
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(2);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.MOVED_NORTH_BY_BELT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());

    gameLogic.expressTwoAndNormalBelts();
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(3);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertRobotPosition(robotOcean, Direction.EAST, 3, 7);
    assertEquals(AnimationType.MOVED_NORTH_BY_BELT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());

    // move Robot one up
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.NORTH));
    robotOcean.setDirection(Direction.NORTH);
    assertRobotPosition(robotOcean, Direction.NORTH, 3, 6);

    gameLogic.expressOne();
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(4);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.MOVED_NORTH_BY_BELT, subSteps.getType());
    assertEquals(Direction.NORTH, subSteps.getDirection());
    subSteps = parallelAnimationStep.getAnimations().get(1);
    assertEquals(AnimationType.TURN_RIGHT, subSteps.getType());
    assertRobotPosition(robotOcean, Direction.EAST, 3, 5);

    gameLogic.expressTwoAndNormalBelts();
    gameLogic.expressOne();
    assertRobotPosition(robotOcean, Direction.EAST, 0, 11); // respawned
    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(5);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.MOVED_EAST_BY_BELT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());
    parallelAnimationStep = (ParallelAnimation) actualStep.get(6);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.PIT, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());
  }

  @Test
  void stepObjectTestPusher() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "197;105;105;094;000;000;000;000;000;000;000;0002\n"
                + "000;000;000;197;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.OCEAN, user);
    lobby.setRobotSelected(Robots.BOB, user2);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot robotOcean = game.getPlayer(user).getRobot();
    Robot robotBob = game.getPlayer(user2).getRobot();
    List<Animation> actualStep;
    SeparationPoint separationPoint;
    ParallelAnimation parallelAnimationStep;
    Animation subSteps;

    ProgrammingCard card = new ProgrammingCard(10, CardType.MOVE1);
    card.setOwner(robotOcean.getType());
    game.getPlayer(user).getRobot().setSelectedCards(new ProgrammingCard[] {card});
    robotOcean.setDirection(Direction.EAST);

    assertRobotPosition(robotOcean, Direction.EAST, 0, 10);

    robotBob.setDirection(Direction.NORTH);
    assertRobotPosition(robotBob, Direction.NORTH, 3, 11);
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.EAST));
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.NORTH));
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.NORTH));
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.WEST));
    assertRobotPosition(robotBob, Direction.NORTH, 3, 9);

    gameLogic.run();

    actualStep = gameLogic.getStep().getSteps();
    assertEquals(actualStep.get(0).getClass(), SeparationPoint.class);
    assertEquals(actualStep.get(1).getClass(), ParallelAnimation.class);

    ParallelAnimation parallelAnimation = (ParallelAnimation) actualStep.get(1);

    separationPoint = (SeparationPoint) parallelAnimation.getAnimations().get(0);
    assertEquals(AnimationType.DISPLAY_NEXT_CARD, separationPoint.getType());

    assertEquals(actualStep.get(2).getClass(), ParallelAnimation.class);
    assertEquals(actualStep.get(7).getClass(), ParallelAnimation.class);

    parallelAnimationStep = (ParallelAnimation) actualStep.get(7);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.MOVED_NORTH, subSteps.getType());
    assertEquals(Robots.BOB, subSteps.getRobot());

    subSteps = parallelAnimationStep.getAnimations().get(1);
    assertEquals(AnimationType.MOVED_NORTH, subSteps.getType());
    assertEquals(Robots.OCEAN, subSteps.getRobot());
  }

  @Test
  void stepObjectGears() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "197;037;000;000;000;000;000;000;000;000;000;0002\n"
                + "197;038;000;000;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("CLEAN", user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.OCEAN, user);
    lobby.setRobotSelected(Robots.BOB, user2);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot robotOcean = game.getPlayer(user).getRobot();
    Robot robotBob = game.getPlayer(user2).getRobot();
    List<Animation> actualStep;
    ParallelAnimation parallelAnimationStep;
    Animation subSteps;

    assertRobotPosition(robotOcean, Direction.EAST, 0, 10);
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotOcean, Direction.EAST, 1, 10);

    assertRobotPosition(robotBob, Direction.EAST, 0, 11);
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotBob, Direction.EAST, 1, 11);

    gameLogic.run();

    assertRobotPosition(robotOcean, Direction.NORTH, 1, 10);

    assertRobotPosition(robotBob, Direction.SOUTH, 1, 11);

    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(7);

    for (int i = 0; i < 2; i++) {
      subSteps = parallelAnimationStep.getAnimations().get(i);
      if (subSteps.getRobot().equals(Robots.OCEAN)) {
        assertEquals(Robots.OCEAN, subSteps.getRobot());
        assertEquals(Direction.EAST, subSteps.getDirection());
        assertEquals(AnimationType.TURN_LEFT, subSteps.getType());
      } else {

        assertEquals(AnimationType.TURN_RIGHT, subSteps.getType());
        assertEquals(Direction.EAST, subSteps.getDirection());
      }
    }
  }

  @Test
  void stepObjectPress() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "197;114;140;140;000;000;000;000;000;000;000;0002\n"
                + "197;114;140;140;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("test", user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.OCEAN, user);
    lobby.setRobotSelected(Robots.BOB, user2);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot robotOcean = game.getPlayer(user).getRobot();
    Robot robotBob = game.getPlayer(user2).getRobot();
    List<Animation> actualStep;
    ParallelAnimation parallelAnimationStep;
    Animation subSteps;
    assertRobotPosition(robotOcean, Direction.EAST, 0, 10);
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotOcean, Direction.EAST, 1, 10);

    assertRobotPosition(robotBob, Direction.EAST, 0, 11);
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotBob, Direction.EAST, 1, 11);

    gameLogic.run();

    int SECOND_PRESSES_ANIMATION = 17;

    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(SECOND_PRESSES_ANIMATION);
    subSteps = parallelAnimationStep.getAnimations().get(0);
    Robots tempRobot = subSteps.getRobot();
    assertEquals(AnimationType.PRESS, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());

    subSteps = parallelAnimationStep.getAnimations().get(1);
    assertEquals(AnimationType.PRESS, subSteps.getType());
    assertEquals(Direction.EAST, subSteps.getDirection());
    assertNotEquals(tempRobot, subSteps.getRobot());

    System.out.println("s");
  }

  @Test
  void stepObjectLaser() {
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "197;048;000;000;000;000;000;000;000;000;000;0002\n"
                + "197;050;000;000;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("ASF", user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.OCEAN, user);
    lobby.setRobotSelected(Robots.BOB, user2);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = createGameLogic(game);
    Robot robotOcean = game.getPlayer(user).getRobot();
    Robot robotBob = game.getPlayer(user2).getRobot();
    List<Animation> actualStep;
    ParallelAnimation parallelAnimationStep;
    Animation subSteps;

    assertRobotPosition(robotOcean, Direction.EAST, 0, 10);
    robotOcean.setPosition(robotOcean.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotOcean, Direction.EAST, 1, 10);

    assertRobotPosition(robotBob, Direction.EAST, 0, 11);
    robotBob.setPosition(robotBob.getPosition().getNeighbour(Direction.EAST));
    assertRobotPosition(robotBob, Direction.EAST, 1, 11);

    gameLogic.run();

    int FIRST_LASER_ANIMATION = 9;

    actualStep = gameLogic.getStep().getSteps();
    parallelAnimationStep = (ParallelAnimation) actualStep.get(FIRST_LASER_ANIMATION);

    subSteps = parallelAnimationStep.getAnimations().get(0);
    assertEquals(AnimationType.LASER_DAMAGE, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(1);
    assertEquals(AnimationType.UPDATE_DAMAGE_AND_LIVES, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(2);
    assertEquals(AnimationType.LASER_DAMAGE, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(3);
    assertEquals(AnimationType.UPDATE_DAMAGE_AND_LIVES, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(4);
    assertEquals(AnimationType.LASER_DAMAGE, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(5);
    assertEquals(AnimationType.UPDATE_DAMAGE_AND_LIVES, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(6);
    assertEquals(AnimationType.LASER_DAMAGE, subSteps.getType());

    subSteps = parallelAnimationStep.getAnimations().get(7);
    assertEquals(AnimationType.UPDATE_DAMAGE_AND_LIVES, subSteps.getType());
  }

  @Test
  void stepObjectCards() {
    Player p1 = game3P.getPlayer(user);
    Player p2 = game3P.getPlayer(user1);
    Player p3 = game3P.getPlayer(user2);

    ProgrammingCard cardP1_1 = new ProgrammingCard(10, CardType.MOVE1);
    ProgrammingCard cardP2_1 = new ProgrammingCard(20, CardType.MOVE1);
    ProgrammingCard cardP3_1 = new ProgrammingCard(30, CardType.MOVE1);

    ProgrammingCard cardP1_2 = new ProgrammingCard(10, CardType.TURN_LEFT);
    ProgrammingCard cardP2_2 = new ProgrammingCard(20, CardType.TURN_LEFT);
    ProgrammingCard cardP3_2 = new ProgrammingCard(30, CardType.TURN_LEFT);

    ProgrammingCard cardP1_3 = new ProgrammingCard(10, CardType.U_TURN);
    ProgrammingCard cardP2_3 = new ProgrammingCard(20, CardType.U_TURN);
    ProgrammingCard cardP3_3 = new ProgrammingCard(30, CardType.U_TURN);

    p1.getRobot().setSelectedCardsTesting(new ProgrammingCard[] {cardP1_1, cardP1_2, cardP1_3});
    p2.getRobot().setSelectedCardsTesting(new ProgrammingCard[] {cardP2_1, cardP2_2, cardP2_3});
    p3.getRobot().setSelectedCardsTesting(new ProgrammingCard[] {cardP3_1, cardP3_2, cardP3_3});

    GameLogic gameLogic = createGameLogic(game3P);
    gameLogic.run();

    List<Animation> actualStep = gameLogic.getStep().getSteps();
    Animation subSteps;
    ParallelAnimation parallelAnimationStep;
    int DISPLAYCARDS = 1;

    parallelAnimationStep = (ParallelAnimation) actualStep.get(DISPLAYCARDS);
    assertEquals(3, parallelAnimationStep.getAnimations().size());

    parallelAnimationStep = (ParallelAnimation) actualStep.get(DISPLAYCARDS + 12);
    assertEquals(3, parallelAnimationStep.getAnimations().size());

    parallelAnimationStep = (ParallelAnimation) actualStep.get(DISPLAYCARDS + 12 + 12);
    assertEquals(3, parallelAnimationStep.getAnimations().size());
  }

  private GameLogic createGameLogic(Game game) {
    return new GameLogic(game, gameService, new EventBus(), true);
  }

  @Test
  void shutdownTest() {
    EventBus bus = new EventBus();
    FloorPlan floorPlan =
        new FloorPlan(
            "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;000;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "000;000;000;005;000;000;000;000;000;000;000;000\n"
                + "197;037;000;000;000;000;000;000;000;000;000;0002\n"
                + "197;038;000;000;000;000;000;000;000;000;000;0001",
            true);

    User user = new UserDTO(UUID.randomUUID().toString(), "a", "a", "a", new UserData(5));
    User user2 = new UserDTO(UUID.randomUUID().toString(), "b", "a", "a", new UserData(5));
    Lobby lobby = new LobbyDTO("ASDF", user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.OCEAN, user);
    lobby.setRobotSelected(Robots.BOB, user2);
    lobby.getOptions().setSwitchOffRoboter(true);

    Game game = new GameDTO(lobby, floorPlan);
    GameLogic gameLogic = new GameLogic(game, gameService, bus, true);
    Robot robotOcean = game.getPlayer(user).getRobot();
    Robot robotBob = game.getPlayer(user2).getRobot();

    robotOcean.damage();
    robotBob.damage();
    robotBob.damage();
    robotOcean.shutdown();
    gameLogic.shutdown();
    assertEquals(0, robotOcean.getDamage());
    assertNotEquals(0, robotBob.getDamage());
  }
}
