package de.uol.swp.server.chat.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.chat.ChatService;
import de.uol.swp.server.game.GameManagement;
import de.uol.swp.server.game.GameService;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.lobby.generator.NameGenerator;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SuppressWarnings("UnstableApiUsage")
class MoveRobotCommandTest {

  EventBus bus;
  ChatMessageDTO chatMessageDTO;
  String command;
  String variable;
  String[][] parameter = {{"e", "e"}};
  ChatService chatService;
  LobbyManagement lobbyManagement = new LobbyManagement();
  GameManagement gameManagement = new GameManagement();
  AuthenticationService authenticationService =
      new AuthenticationService(new EventBus(), new UserManagement(new MainMemoryBasedUserStore()));
  LobbyService lobbyService;
  GameService gameService;
  UserDTO user = new UserDTO("uuid", "Genesung", "1234", "mail@.de", new UserData(1));
  String lobbyId = "hhhh";
  LobbyDTO lobbyDTO;
  FloorPlan floorPlan =
      new FloorPlan(
          "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;097;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;000\n"
              + "000;000;000;000;000;000;000;000;000;000;000;0002\n"
              + "000;000;000;000;000;000;000;000;000;000;000;0001",
          true);
  GameDTO gameDTO;
  Robot robot;
  FloorField floorField;
  Direction direction;

  @BeforeEach
  void setUp() {

    bus = mock(EventBus.class);

    lobbyManagement = LobbyManagement.getInstance();
    lobbyService = new LobbyService(lobbyManagement, authenticationService, bus);
    gameService = new GameService(bus, gameManagement, lobbyManagement, lobbyService, 5000);

    chatService = ChatService.getInstance(bus, lobbyService);

    lobbyManagement.createLobby(lobbyId, user, new LobbyOptions());
    lobbyDTO = lobbyManagement.getLobbyDTO(lobbyId);
    lobbyDTO.setRobotSelected(Robots.BOB, user);
    gameDTO = new GameDTO(lobbyDTO, floorPlan);
    gameManagement.createGameFromGameDTO(lobbyDTO, gameDTO);
    Optional<Game> optionalGame = gameManagement.getGame(lobbyId);
    if (optionalGame.isEmpty()) {
      throw new NullPointerException();
    }
    robot = optionalGame.get().getPlayer(user).getRobot();
    floorField = robot.getPosition();
    direction = robot.getDirection();
  }

  @AfterEach
  void tearDown() {
    Optional<User> user = lobbyDTO.getUsers().stream().findFirst();
    if (user.isEmpty()) {
      fail();
    }
    lobbyDTO.leaveUser(user.get());
    lobbyManagement.dropLobby(lobbyId);
    Mockito.reset(bus);
    NameGenerator.markAsUnused(lobbyId);
  }

  @Test
  void moveRobotCommand_north() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "n";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField.getNeighbour(Direction.NORTH), robot.getPosition());
  }

  @Test
  void moveRobotCommand_south() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "s";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField.getNeighbour(Direction.SOUTH), robot.getPosition());
  }

  @Test
  void moveRobotCommand_west() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "w";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField.getNeighbour(Direction.WEST), robot.getPosition());
  }

  @Test
  void moveRobotCommand_east() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "e";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField.getNeighbour(Direction.EAST), robot.getPosition());
  }

  @Test
  void moveRobotCommand_rotateLeft() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "l";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField, robot.getPosition());
    assertEquals(direction.rotate(Direction.WEST), robot.getDirection());
  }

  @Test
  void moveRobotCommand_rotateRight() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "r";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField, robot.getPosition());
    assertEquals(direction.rotate(Direction.EAST), robot.getDirection());
  }

  @Test
  void moveRobotCommand_rotateUp() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "u";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField, robot.getPosition());
    assertEquals(direction.rotate(Direction.SOUTH), robot.getDirection());
  }

  @Test
  void moveRobotCommand_wrongInput() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "353vau";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField, robot.getPosition());
    assertEquals(direction, robot.getDirection());
  }

  @Test
  void moveRobotCommand_emptyInput() {
    chatMessageDTO = new ChatMessageDTO(user, "", lobbyId);
    variable = "";
    MoveRobotCommand moveRobotCommand = new MoveRobotCommand(bus);
    moveRobotCommand.execute(chatMessageDTO, command, variable, parameter, chatService, null);

    assertEquals(floorField, robot.getPosition());
    assertEquals(direction, robot.getDirection());
  }
}
