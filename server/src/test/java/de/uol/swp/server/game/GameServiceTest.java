package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.message.GameStartMessage;
import de.uol.swp.common.game.message.GuiIsFinishMessage;
import de.uol.swp.common.game.message.ProgrammingCardMessage;
import de.uol.swp.common.game.message.RespawnDirectionInteractionMessage;
import de.uol.swp.common.game.message.RespawnInteractionMessage;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.message.RoundFinishMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.request.GameDTORequest;
import de.uol.swp.common.game.request.NextRoundStartForceRequest;
import de.uol.swp.common.game.request.RespawnDirectionInteractionRequest;
import de.uol.swp.common.game.request.RespawnInteractionRequest;
import de.uol.swp.common.game.request.StartGameRequest;
import de.uol.swp.common.game.request.UnblockCardRequest;
import de.uol.swp.common.game.response.GameDTOResponse;
import de.uol.swp.common.game.response.UnblockCardResponse;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.message.AbstractMessage;
import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.lobby.generator.NameGenerator;
import de.uol.swp.server.message.GameLogicFinish;
import de.uol.swp.server.message.ReplaceUserWithAiMessage;
import de.uol.swp.server.message.RespawnInteractionTimeMessage;
import de.uol.swp.server.message.StartRespawnInteractionInternalMessage;
import de.uol.swp.server.message.WaitOnGuiTimerMessage;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@SuppressWarnings("UnstableApiUsage")
class GameServiceTest {
  Deque<Robot> robots = new ArrayDeque<>();
  EventBus eventBus;
  AuthenticationService authenticationService =
      new AuthenticationService(new EventBus(), new UserManagement(new MainMemoryBasedUserStore()));
  GameService gameService;
  LobbyService lobbyService;
  LobbyManagement lobbyManagement = new LobbyManagement();
  GameManagement gameManagement = new GameManagement();
  String lobbyId = "JETE";
  Lobby lobby;
  UserDTO user;
  UserDTO user2;
  Game game;

  @BeforeEach
  void setUp() {
    eventBus = mock(EventBus.class);
    lobbyManagement = LobbyManagement.getInstance();
    lobbyService = new LobbyService(lobbyManagement, authenticationService, eventBus);
    gameService = new GameService(eventBus, gameManagement, lobbyManagement, lobbyService, 5000);
    user =
        new UserDTO(
            UUID.randomUUID().toString(),
            "Gamer1",
            "swpBestesProjekt",
            "mail@uni.de",
            new UserData(2));
    user2 =
        new UserDTO(
            UUID.randomUUID().toString(),
            "Gamer2",
            "swpBestesProjekt",
            "mail@uni.de",
            new UserData(1));
    if (lobbyManagement.getLobby(lobbyId).isPresent()) {
      lobbyManagement.dropLobby(lobbyId);
    }
    lobbyManagement.createLobby(lobbyId, user, new LobbyOptions());
    lobby = lobbyManagement.getLobbyDTO(lobbyId);
    lobby.setRobotSelected(Robots.BOB, user);
    lobby.joinUser(user2);
    lobby.setRobotSelected(Robots.GANDALF, user2);
    lobby
        .getOptions()
        .setFloorPlanSettings(
            new FloorPlanSetting[] {
              new FloorPlanSetting(FloorPlans.ISLAND, Direction.EAST),
                  new FloorPlanSetting(FloorPlans.EMPTY),
              new FloorPlanSetting(FloorPlans.EMPTY), new FloorPlanSetting(FloorPlans.EMPTY)
            });
    Map<Integer, EnumMap<FloorPlans, Point>> checkpoint = new HashMap<>();
    checkpoint.put(1, new EnumMap<>(Map.of(FloorPlans.ISLAND, new Point(0, 0))));
    checkpoint.put(2, new EnumMap<>(Map.of(FloorPlans.ISLAND, new Point(0, 5))));
    lobby.getOptions().setCheckpointsPosition(checkpoint);
    gameManagement.createGame(Optional.of(lobby));
    // Exception is OK here if game does not exist!
    game = gameService.getGame(lobbyId).get();
  }

  @AfterEach
  void tearDown() {
    lobby = lobbyManagement.getLobbyDTO(lobbyId);
    if (lobby != null) {
      lobby.leaveUser(lobby.getUsers().stream().findFirst().get());
    }
    if (gameManagement.getGame(lobbyId).isPresent()) {
      gameManagement.dropGame(lobbyId);
    }
    if (lobbyManagement.getLobby(lobbyId).isPresent()) {
      lobbyManagement.dropLobby(lobbyId);
    }
    NameGenerator.markAsUnused(lobbyId);
  }

  void ende() {
    lobby = lobbyManagement.getLobbyDTO(lobbyId);
    lobby.leaveUser(lobby.getUsers().stream().findFirst().get());
    lobby.leaveUser(lobby.getUsers().stream().findFirst().get());
    try {
      gameManagement.dropGame(lobbyId);
    } catch (IllegalArgumentException e) {
    }
    lobbyManagement.dropLobby(lobbyId);
    lobby = null;
    NameGenerator.markAsUnused(lobbyId);
  }

  // -------------------------------------------------------------------------------

  @Test
  void onStartGameRequest() {
    StartGameRequest startGameRequestOwner = new StartGameRequest(lobbyId, user);
    StartGameRequest startGameRequestOther = new StartGameRequest(lobbyId, user2);
    // non-owner should not be able to start a game
    gameService.onStartGameRequest(startGameRequestOther);
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // owner is allowed to start a game - but not one already running
    //
    assertTrue(gameService.getGame(lobbyId).isPresent());
    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> gameService.onStartGameRequest(startGameRequestOwner));
    assertEquals("Lobby " + lobbyId + " is already assigned to a game!", e.getMessage());
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // owner can start a new game
    //
    gameManagement.dropGame(lobbyId);
    gameService.onStartGameRequest(startGameRequestOwner);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(eventBus, Mockito.times(2)).post(msgCaptor.capture());
    List<Message> messages = msgCaptor.getAllValues();
    //
    assertTrue(messages.get(0) instanceof UpdateLobbyListMessage);
    UpdateLobbyListMessage msgUpdList = (UpdateLobbyListMessage) messages.get(0);
    assertEquals(lobbyId, msgUpdList.getLobbyId());
    assertEquals(user.getUsername(), msgUpdList.getOwnerName());
    assertEquals(LobbyOptions.LobbyStatus.INGAME, msgUpdList.getStatus());
    //
    assertTrue(messages.get(1) instanceof GameStartMessage);
    GameStartMessage msgGameStart = (GameStartMessage) messages.get(1);
    assertEquals(lobbyId, msgGameStart.getGameId());
  }

  @Test
  void onGameDTORequest() {
    ArgumentCaptor<GameDTOResponse> captorResponse = ArgumentCaptor.forClass(GameDTOResponse.class);
    //
    // Bad Lobby
    gameService.onGameDTORequest(new GameDTORequest("LOBY"));
    verify(eventBus, Mockito.times(1)).post(captorResponse.capture());
    GameDTOResponse responseGameDTO = (GameDTOResponse) captorResponse.getValue();
    assertEquals(null, responseGameDTO.getGameDTO(), "Bad Lobby -> response without GameDTO");
    //
    // Good lobby with game
    Optional<Game> gameOptional = gameService.getGame(lobbyId);
    assertTrue(gameOptional.isPresent());
    Game game = gameOptional.get();
    gameService.onGameDTORequest(new GameDTORequest(lobbyId));
    //
    verify(eventBus, Mockito.times(2)).post(captorResponse.capture());
    responseGameDTO = (GameDTOResponse) captorResponse.getValue();
    assertEquals(game, responseGameDTO.getGameDTO(), "Good lobby with active game");
    //
    // Good lobby without game
    gameOptional = gameService.getGame(lobbyId);
    assertTrue(gameOptional.isPresent());
    gameManagement.dropGame(lobbyId);
    gameService.onGameDTORequest(new GameDTORequest(lobbyId));
    //
    verify(eventBus, Mockito.times(3)).post(captorResponse.capture());
    responseGameDTO = (GameDTOResponse) captorResponse.getValue();
    assertEquals(
        null, responseGameDTO.getGameDTO(), "Good lobby without game -> response without GameDTO");
  }

  @Test
  void onUnblockCardRequest() {
    Robot robot = game.getPlayer(user).getRobot();

    boolean[] noBlockedSlots = new boolean[] {false, false, false, false, false};
    boolean[] oneBlockedSlot = new boolean[] {false, false, false, false, true};
    //
    // Bad GameID
    gameService.onUnblockCardRequest(new UnblockCardRequest("Gamer1", "LOBY", 4));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID, bad player name
    gameService.onUnblockCardRequest(new UnblockCardRequest("user1", lobbyId, 4));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID
    //
    ArgumentCaptor<UnblockCardResponse> responseCaptor =
        ArgumentCaptor.forClass(UnblockCardResponse.class);
    UnblockCardResponse response;
    //
    // Robot damaged
    robot.setBlocked(oneBlockedSlot);
    gameService.onUnblockCardRequest(new UnblockCardRequest("Gamer1", lobbyId, 4));
    verify(eventBus, Mockito.times(1)).post(responseCaptor.capture());
    response = responseCaptor.getValue();
    //
    assertEquals(user, response.getUser());
    assertEquals(lobbyId, response.getGameId());
    assertArrayEquals(noBlockedSlots, response.getBlocked());
    //
    // Robots undamaged
    robot.setBlocked(noBlockedSlots);
    gameService.onUnblockCardRequest(new UnblockCardRequest("Gamer1", lobbyId, 4));
    verify(eventBus, Mockito.times(2)).post(responseCaptor.capture());
    response = responseCaptor.getValue();
    //
    assertEquals(user, response.getUser());
    assertEquals(lobbyId, response.getGameId());
    assertArrayEquals(noBlockedSlots, response.getBlocked());
    //
    // Valid lobby, without game
    Mockito.reset(eventBus);
    Optional<Game> gameOptional = gameService.getGame(lobbyId);
    assertTrue(gameOptional.isPresent());
    gameManagement.dropGame(lobbyId);
    gameService.onUnblockCardRequest(new UnblockCardRequest("Gamer1", lobbyId, 4));
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // -------------------------------------------------------------------------------
  // sendProgramingCards
  // -------------------------------------------------------------------------------

  @Test
  void sendProgramingCards() {
    Player player1 = game.getPlayer(user);
    Player player2 = game.getPlayer(user2);
    Robot robot1 = player1.getRobot();
    Robot robot2 = player2.getRobot();
    int damage1 = (Robot.MEMORY_SIZE - Robot.PROGRAM_SIZE);
    int damage2 = (Robot.MEMORY_SIZE - Robot.PROGRAM_SIZE) + 1;
    boolean[] noBlockedSlots = new boolean[] {false, false, false, false, false};
    boolean[] oneBlockedSlot = new boolean[] {false, false, false, false, true};
    //
    ArgumentCaptor<ProgrammingCardMessage> captorProgCards =
        ArgumentCaptor.forClass(ProgrammingCardMessage.class);
    ProgrammingCardMessage msgProgCards;
    //
    // Bad GameID
    gameService.sendProgramingCards("LOBY");
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID
    //
    // Robots undamaged
    assertEquals(0, robot1.getDamage());
    assertArrayEquals(noBlockedSlots, robot1.getBlocked());
    assertEquals(0, robot2.getDamage());
    assertArrayEquals(noBlockedSlots, robot2.getBlocked());
    ((GameDTO) game).setTimerStopped(true);
    //
    gameService.sendProgramingCards(lobbyId);
    assertFalse(((GameDTO) game).isTimerStopped());
    verify(eventBus, Mockito.times(1)).post(captorProgCards.capture());
    msgProgCards = (ProgrammingCardMessage) captorProgCards.getValue();
    assertProgrammingCardMessage(player1, robot1, 0, msgProgCards);
    assertProgrammingCardMessage(player2, robot2, 0, msgProgCards);
    //
    // Robots damaged
    for (int i = 0; i < damage1; i++) {
      robot1.damage();
    }
    assertEquals(damage1, robot1.getDamage());
    assertArrayEquals(noBlockedSlots, robot1.getBlocked());
    for (int i = 0; i < damage2; i++) {
      robot2.damage();
    }
    assertEquals(damage2, robot2.getDamage());
    assertArrayEquals(oneBlockedSlot, robot2.getBlocked());
    ((GameDTO) game).setTimerStopped(true);
    //
    gameService.sendProgramingCards(lobbyId);
    assertFalse(((GameDTO) game).isTimerStopped());
    verify(eventBus, Mockito.times(2)).post(captorProgCards.capture());
    msgProgCards = (ProgrammingCardMessage) captorProgCards.getValue();
    assertProgrammingCardMessage(player1, robot1, damage1, msgProgCards);
    assertProgrammingCardMessage(player2, robot2, damage2, msgProgCards);
  }

  private void assertProgrammingCardMessage(
      Player player, Robot robot, int damage, ProgrammingCardMessage msgProgCards) {
    Robots robotType = robot.getType();
    assertEquals(
        Robot.MEMORY_SIZE - damage,
        msgProgCards.getCards(player).length,
        "Set of cards, reduced by damage");
    for (int i = 0; i < Robot.MEMORY_SIZE; i++) {
      if (i < Robot.MEMORY_SIZE - damage) {
        assertEquals(
            msgProgCards.getCards(player)[i], robot.getHandCards()[i], "Undamaged cards match");
        assertEquals(robotType, msgProgCards.getCards(player)[i].getOwner());
        assertEquals(robotType, robot.getHandCards()[i].getOwner());
      } else {
        assertEquals(null, robot.getHandCards()[i], "Cards behind damage are empty");
      }
    }
    assertArrayEquals(robot.getBlocked(), msgProgCards.getBlocked(player));
  }

  // -------------------------------------------------------------------------------

  @Test
  void onReplaceRobotWithAiMessage() {
    Optional<Game> optionalGame = gameManagement.getGame(lobbyId);
    if (optionalGame.isPresent()) {
      Game game = optionalGame.get();
      gameService.onReplaceUserWithAiMessage(new ReplaceUserWithAiMessage(user2, lobbyId));
      assertEquals(1, game.getLobby().getOptions().getAiPlayerCount());
      assertEquals(1, game.getPlayers().stream().filter(Player::isAiPlayer).count());
    }
  }

  // -------------------------------------------------------------------------------
  // onGameLogicFinish
  // -------------------------------------------------------------------------------

  @Test
  void onGameLogicFinish() {
    //
    // Game with winner
    game.setWinner(game.getPlayer(user));
    gameService.onGameLogicFinish(new GameLogicFinish(new Step(lobbyId), lobby));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Game without winner
    Step testSteps = new Step(lobbyId);
    GameLogicFinish gameLogicFinish = new GameLogicFinish(testSteps, lobby);
    assertEquals(lobby, gameLogicFinish.getLobby());
    assertEquals(testSteps, gameLogicFinish.getStep());
    //
    game.setWinner(null);
    gameService.onGameLogicFinish(gameLogicFinish);

    ArgumentCaptor<RoundFinishMessage> eventCaptor =
        ArgumentCaptor.forClass(RoundFinishMessage.class);
    verify(eventBus).post(eventCaptor.capture());
    RoundFinishMessage capture = eventCaptor.getValue();

    assertEquals(testSteps, capture.getStep());
    assertTrue(game.isInGuiShowPhase());
    assertTrue(game.isInFirstRoundOfRespawnProcess());
  }

  @Test
  void onGameLogicFinish_noGame() {
    //
    // Good lobby without game
    assertTrue(gameService.getGame(lobbyId).isPresent());
    gameManagement.dropGame(lobbyId);
    gameService.onGameLogicFinish(new GameLogicFinish(new Step(lobbyId), lobby));
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // -------------------------------------------------------------------------------
  // onWaitOnGuiTimerMessage
  // -------------------------------------------------------------------------------

  @Test
  void onWaitOnGuiTimerMessage() {
    //
    // Bad GameID
    gameService.onWaitOnGuiTimerMessage(new WaitOnGuiTimerMessage("LOBY"));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID, but not in GuiShowPhase
    game.setInGuiShowPhase(false);
    gameService.onWaitOnGuiTimerMessage(new WaitOnGuiTimerMessage(lobbyId));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID, in GuiShowPhase
    game.setInGuiShowPhase(true);
    gameService.onWaitOnGuiTimerMessage(new WaitOnGuiTimerMessage(lobbyId));

    ArgumentCaptor<StartRespawnInteractionInternalMessage> eventCaptor =
        ArgumentCaptor.forClass(StartRespawnInteractionInternalMessage.class);

    verify(eventBus).post(eventCaptor.capture());
    StartRespawnInteractionInternalMessage capture = eventCaptor.getValue();

    assertEquals(lobbyId, capture.getGameId());
    ende();
  }

  // -------------------------------------------------------------------------------
  // onStartRespawnInteractionInternalMessage
  // -------------------------------------------------------------------------------

  @Test
  void onStartRespawnInteractionInternalMessage() {
    game.getPlayers()
        .forEach(
            (player -> player.getRobot().setPosition(game.getFloorPlan().getFloorFields(4, 0))));

    game.getPlayers().stream()
        .sorted(Comparator.comparing(p -> p.getUser().getUsername()))
        .forEach((player -> robots.add(player.getRobot())));
    game.setRobotOnRoundRespawnQueue(
        new HashMap(
            Map.of(
                game.getFloorPlan().getFloorFields(4, 0),
                robots.stream()
                    .sorted(Comparator.comparing(r -> game.getPlayer(r).getUser().getUsername()))
                    .collect(Collectors.toCollection(ArrayDeque::new)))));
    ArgumentCaptor<AbstractServerMessage> eventCaptor =
        ArgumentCaptor.forClass(AbstractServerMessage.class);

    gameService.onStartRespawnInteractionInternalMessage(
        new StartRespawnInteractionInternalMessage(lobbyId));

    Mockito.verify(eventBus, Mockito.times(2)).post(eventCaptor.capture());
    List<AbstractServerMessage> reqMsg = eventCaptor.getAllValues();

    RespawnDirectionInteractionMessage msg1 = (RespawnDirectionInteractionMessage) reqMsg.get(0);
    RespawnInteractionMessage msg2 = (RespawnInteractionMessage) reqMsg.get(1);

    assertEquals("Gamer1", msg1.getUser().getUsername());
    assertEquals("Gamer2", msg2.getUser().getUsername());
  }

  @Test
  @SuppressWarnings("java:S5845")
  void onStartRespawnInteractionInternalMessage_EmptyGame() {
    StartRespawnInteractionInternalMessage startRespawnInteractionInternalMessage =
        new StartRespawnInteractionInternalMessage("lobbyID");
    assertEquals("lobbyID", startRespawnInteractionInternalMessage.getGameId());
    assertEquals(
        "StartRespawnInteractionInternalMessage{gameName='" + "lobbyID" + "'}",
        startRespawnInteractionInternalMessage.toString());
    assertNotEquals(null, startRespawnInteractionInternalMessage);
    assertNotEquals(10, startRespawnInteractionInternalMessage);
    StartRespawnInteractionInternalMessage startRespawnInteractionInternalMessage2 =
        new StartRespawnInteractionInternalMessage("lobbyID");
    assertEquals(startRespawnInteractionInternalMessage2, startRespawnInteractionInternalMessage);
    assertEquals(
        startRespawnInteractionInternalMessage2.hashCode(),
        startRespawnInteractionInternalMessage.hashCode());

    gameService.onStartRespawnInteractionInternalMessage(startRespawnInteractionInternalMessage);
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onStartRespawnInteractionInternalMessage_NoRobotsInQueue() {
    gameService.onStartRespawnInteractionInternalMessage(
        new StartRespawnInteractionInternalMessage(lobbyId));
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onStartRespawnInteractionInternalMessage_OnlyOneRobotRespawn() {
    game.setInFirstRoundOfRespawnProcess(true);

    game.getPlayers()
        .forEach(
            (player -> player.getRobot().setPosition(game.getFloorPlan().getFloorFields(4, 0))));

    game.getPlayers().stream()
        .sorted(Comparator.comparing(p -> p.getUser().getUsername()))
        .forEach((player -> robots.add(player.getRobot())));
    game.setRobotOnRoundRespawnQueue(
        new HashMap(
            Map.of(
                game.getFloorPlan().getFloorFields(4, 0),
                new ArrayDeque<>(Collections.singletonList(game.getPlayer(user).getRobot())))));

    ArgumentCaptor<AbstractServerMessage> eventCaptor =
        ArgumentCaptor.forClass(AbstractServerMessage.class);

    gameService.onStartRespawnInteractionInternalMessage(
        new StartRespawnInteractionInternalMessage(lobbyId));

    Mockito.verify(eventBus, Mockito.times(1)).post(eventCaptor.capture());
    AbstractServerMessage reqMsg = eventCaptor.getValue();

    RespawnDirectionInteractionMessage msg1 = (RespawnDirectionInteractionMessage) reqMsg;

    assertEquals("Gamer1", msg1.getUser().getUsername());
  }

  @Test
  void onStartRespawnInteractionInternalMessage_NoGuiIsInteractionIsNeeded() {
    game.setInFirstRoundOfRespawnProcess(false);

    game.getPlayers()
        .forEach(
            (player -> player.getRobot().setPosition(game.getFloorPlan().getFloorFields(4, 0))));

    game.getPlayers().stream()
        .sorted(Comparator.comparing(p -> p.getUser().getUsername()))
        .forEach((player -> robots.add(player.getRobot())));
    game.setRobotOnRoundRespawnQueue(
        new HashMap(
            Map.of(
                game.getFloorPlan().getFloorFields(4, 0),
                robots.stream()
                    .sorted(Comparator.comparing(r -> game.getPlayer(r).getUser().getUsername()))
                    .collect(Collectors.toCollection(ArrayDeque::new)))));

    ArgumentCaptor<AbstractServerMessage> eventCaptor =
        ArgumentCaptor.forClass(AbstractServerMessage.class);

    gameService.onStartRespawnInteractionInternalMessage(
        new StartRespawnInteractionInternalMessage(lobbyId));

    Mockito.verify(eventBus, Mockito.times(1)).post(eventCaptor.capture());
    AbstractServerMessage reqMsg = eventCaptor.getValue();

    RespawnInteractionMessage msg1 = (RespawnInteractionMessage) reqMsg;

    assertEquals("Gamer1", msg1.getUser().getUsername());
  }

  // -------------------------------------------------------------------------------
  // onGuiIsFinishMessage
  // -------------------------------------------------------------------------------

  @Test
  @SuppressWarnings({"java:S5845", "java:S3415", "java:S5863"})
  void onGuiIsFinishMessage() {
    game.setInGuiShowPhase(true);
    game.getGuiIsNotAnymoreInShowPhase().add(game.getPlayer(user2));

    ArgumentCaptor<WaitOnGuiTimerMessage> eventCaptor =
        ArgumentCaptor.forClass(WaitOnGuiTimerMessage.class);

    GuiIsFinishMessage guiIsFinishMessage =
        new GuiIsFinishMessage(game.getLobby().getOwner(), lobbyId);
    gameService.onGuiIsFinishMessage(guiIsFinishMessage);

    verify(eventBus).post(eventCaptor.capture());
    WaitOnGuiTimerMessage capture = eventCaptor.getValue();

    assertEquals(lobbyId, capture.getGameId());
    assertEquals("WaitOnGuiTimerMessage{gameName='" + lobbyId + "'}", capture.toString());
    assertEquals(capture, capture);
    assertNotEquals(capture, null);
    assertNotEquals(capture, 10);
    WaitOnGuiTimerMessage testMessage = new WaitOnGuiTimerMessage(lobbyId);
    assertEquals(capture, testMessage);
    assertEquals(capture.hashCode(), testMessage.hashCode());
  }

  @Test
  void onGuiIsFinishMessage_NoGameFound() {
    GuiIsFinishMessage guiIsFinishMessage =
        new GuiIsFinishMessage(game.getLobby().getOwner(), lobbyId);
    gameService.onGuiIsFinishMessage(guiIsFinishMessage);
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onGuiIsFinishMessage_PlayerIsNotInGuiPhase() {
    game.setInGuiShowPhase(false);

    GuiIsFinishMessage guiIsFinishMessage =
        new GuiIsFinishMessage(game.getLobby().getOwner(), "lobbyID");
    gameService.onGuiIsFinishMessage(guiIsFinishMessage);
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onGuiIsFinishMessage_NotAllFinishedWithGui() {
    game.getPlayer(user).getRobot().setNeedGuiInteraction(true);
    game.getPlayer(user2).getRobot().setNeedGuiInteraction(true);
    game.setInGuiShowPhase(true);

    GuiIsFinishMessage guiIsFinishMessage = new GuiIsFinishMessage(user2, "lobbyID");
    gameService.onGuiIsFinishMessage(guiIsFinishMessage);
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onGuiIsFinishMessage_PlayerHasNoGuiInteractionMore() {
    game.setInGuiShowPhase(true);

    User[] users = game.getLobby().getUsers().toArray(de.uol.swp.common.user.User[]::new);
    game.getGuiIsNotAnymoreInShowPhase().add(game.getPlayer(users[1]));
    game.getGuiIsNotAnymoreInShowPhase().add(game.getPlayer(users[0]));

    GuiIsFinishMessage guiIsFinishMessage =
        new GuiIsFinishMessage(game.getLobby().getOwner(), lobbyId);
    gameService.onGuiIsFinishMessage(guiIsFinishMessage);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // -------------------------------------------------------------------------------
  // onRespawnInteractionRequest
  // -------------------------------------------------------------------------------

  @Test
  void onRespawnInteractionRequest() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);

    ArgumentCaptor<AbstractMessage> eventCaptor = ArgumentCaptor.forClass(AbstractMessage.class);

    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest(lobbyId, game.getLobby().getOwner(), 5, Direction.SOUTH);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    verify(eventBus, Mockito.times(2)).post(eventCaptor.capture());

    List<AbstractMessage> reqMsgs = eventCaptor.getAllValues();

    RobotInformationMessage robotInformationMessage = (RobotInformationMessage) reqMsgs.get(0);
    StartRespawnInteractionInternalMessage startRespawnInteractionInternalMessage =
        (StartRespawnInteractionInternalMessage) reqMsgs.get(1);

    assertEquals(lobbyId, startRespawnInteractionInternalMessage.getGameId());
  }

  @Test
  void onRespawnInteractionRequest_NoPlayerInteraction() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);
    game.getPlayers()
        .forEach(
            player -> {
              Robot robot = player.getRobot();
              robot.setPosition(game.getFloorPlan().getFloorFields(1, 1));
              robot.setRespawn(game.getFloorPlan().getFloorFields(1, 0));
            });

    ArgumentCaptor<AbstractMessage> eventCaptor = ArgumentCaptor.forClass(AbstractMessage.class);
    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest(lobbyId, game.getLobby().getOwner(), -1, null);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    Mockito.verify(eventBus, Mockito.times(3)).post(eventCaptor.capture());

    List<AbstractMessage> reqMsgs = eventCaptor.getAllValues();

    ChatMessageMessage chatMessageMessage = (ChatMessageMessage) reqMsgs.get(0);
    assertTrue(
        chatMessageMessage
            .getChatMessage()
            .getMessage()
            .contains(game.getLobby().getOwner().getUsername()));
    RobotInformationMessage robotInformationMessage = (RobotInformationMessage) reqMsgs.get(1);
    StartRespawnInteractionInternalMessage respawnInteractionRequestCapture =
        (StartRespawnInteractionInternalMessage) reqMsgs.get(2);

    assertEquals(lobbyId, respawnInteractionRequestCapture.getGameId());
  }

  @Test
  void onRespawnInteractionRequest_RobotDied() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);
    game.getPlayers()
        .forEach(
            player -> {
              Robot robot = player.getRobot();
              robot.setPosition(game.getFloorPlan().getFloorFields(1, 1));
              robot.setRespawn(game.getFloorPlan().getFloorFields(1, 0));
            });

    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest(lobbyId, game.getLobby().getOwner(), 1, Direction.SOUTH);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    assertEquals(2, game.getPlayer(user).getRobot().getLives());
  }

  @Test
  void onRespawnInteractionRequest_NoGamePresent() {

    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest("lobbyID", user, 1, Direction.SOUTH);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnInteractionRequest_NoGuiInteractionNeeded() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(false);

    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest(lobbyId, game.getLobby().getOwner(), 1, Direction.SOUTH);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnInteractionRequest_NoAllInteractionFromUserIsFinished() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);

    game.getPlayer(user).getRobot().setNeedGuiInteraction(false);
    game.getPlayer(user2).getRobot().setNeedGuiInteraction(false);

    RespawnInteractionRequest respawnInteractionRequest =
        new RespawnInteractionRequest(lobbyId, user, 1, Direction.SOUTH);
    gameService.onRespawnInteractionRequest(respawnInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  // -------------------------------------------------------------------------------
  // onRespawnDirectionInteractionRequest
  // -------------------------------------------------------------------------------

  @Test
  void onRespawnDirectionInteractionRequest() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);

    ArgumentCaptor<AbstractMessage> eventCaptor = ArgumentCaptor.forClass(AbstractMessage.class);

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        new RespawnDirectionInteractionRequest(
            lobbyId, game.getLobby().getOwner(), Direction.SOUTH);
    gameService.onRespawnDirectionInteractionRequest(respawnDirectionInteractionRequest);

    Mockito.verify(eventBus, Mockito.times(2)).post(eventCaptor.capture());

    List<AbstractMessage> reqMsgs = eventCaptor.getAllValues();

    RobotInformationMessage robotInformationMessage = (RobotInformationMessage) reqMsgs.get(0);
    StartRespawnInteractionInternalMessage startRespawnInteractionInternalMessage =
        (StartRespawnInteractionInternalMessage) reqMsgs.get(1);

    assertEquals(lobbyId, startRespawnInteractionInternalMessage.getGameId());
  }

  @Test
  void onRespawnDirectionInteractionRequest_NoGamePresent() {

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        new RespawnDirectionInteractionRequest("lobbyID", user, Direction.SOUTH);
    gameService.onRespawnDirectionInteractionRequest(respawnDirectionInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnDirectionInteractionRequest_NoGuiInteractionNeeded() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(false);

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        new RespawnDirectionInteractionRequest(
            lobbyId, game.getLobby().getOwner(), Direction.SOUTH);
    gameService.onRespawnDirectionInteractionRequest(respawnDirectionInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnDirectionInteractionRequest_NoAllInteractionFromUserIsFinished() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);

    game.getPlayer(user).getRobot().setNeedGuiInteraction(false);
    game.getPlayer(user2).getRobot().setNeedGuiInteraction(false);

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        new RespawnDirectionInteractionRequest(lobbyId, user, Direction.SOUTH);
    gameService.onRespawnDirectionInteractionRequest(respawnDirectionInteractionRequest);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnDirectionInteractionRequest_NoPlayerInteraction() {
    game.getPlayer(game.getLobby().getOwner()).getRobot().setNeedGuiInteraction(true);

    ArgumentCaptor<AbstractMessage> eventCaptor = ArgumentCaptor.forClass(AbstractMessage.class);

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        new RespawnDirectionInteractionRequest(lobbyId, game.getLobby().getOwner(), null);
    gameService.onRespawnDirectionInteractionRequest(respawnDirectionInteractionRequest);

    Mockito.verify(eventBus, Mockito.times(2)).post(eventCaptor.capture());

    List<AbstractMessage> reqMsgs = eventCaptor.getAllValues();

    RobotInformationMessage robotInformationMessage = (RobotInformationMessage) reqMsgs.get(0);
    StartRespawnInteractionInternalMessage startRespawnInteractionInternalMessage =
        (StartRespawnInteractionInternalMessage) reqMsgs.get(1);

    assertEquals(lobbyId, startRespawnInteractionInternalMessage.getGameId());
  }

  // -------------------------------------------------------------------------------
  // onRespawnInteractionTimeMessage
  // -------------------------------------------------------------------------------

  @Test
  void onRespawnInteractionTimeMessage() {
    game.setInGuiShowPhase(true);
    Player[] players =
        game.getPlayers().stream()
            .sorted(Comparator.comparing(p -> p.getUser().getUsername()))
            .toArray(Player[]::new);
    game.getPlayerDirectionInteraction().add(players[1]);
    game.getPlayerFloorFieldInteraction().add(players[0]);

    ArgumentCaptor<AbstractRequestMessage> eventCaptor =
        ArgumentCaptor.forClass(AbstractRequestMessage.class);

    gameService.onRespawnInteractionTimeMessage(new RespawnInteractionTimeMessage(lobbyId));

    Mockito.verify(eventBus, Mockito.times(2)).post(eventCaptor.capture());

    List<AbstractRequestMessage> reqMsgs = eventCaptor.getAllValues();

    int req1 = 0;
    int req2 = 1;
    if (reqMsgs.get(0) instanceof RespawnInteractionRequest) {
      req1 = 1;
      req2 = 0;
    }
    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        (RespawnDirectionInteractionRequest) reqMsgs.get(req1);
    RespawnInteractionRequest respawnInteractionRequest =
        (RespawnInteractionRequest) reqMsgs.get(req2);

    assertEquals("Gamer2", respawnDirectionInteractionRequest.getUser().getUsername());
    assertEquals("Gamer1", respawnInteractionRequest.getUser().getUsername());
  }

  @Test
  @SuppressWarnings("java:S5845")
  void onRespawnInteractionTimeMessage_NoGamePresent() {
    RespawnInteractionTimeMessage respawnInteractionTimeMessage =
        new RespawnInteractionTimeMessage("lobbyID");
    assertEquals("lobbyID", respawnInteractionTimeMessage.getGameId());
    assertEquals(
        "RespawnInteractionTimeMessage{gameName='" + "lobbyID" + "'}",
        respawnInteractionTimeMessage.toString());
    assertNotEquals(null, respawnInteractionTimeMessage);
    assertNotEquals(10, respawnInteractionTimeMessage);
    RespawnInteractionTimeMessage respawnInteractionTimeMessage2 =
        new RespawnInteractionTimeMessage("lobbyID");
    assertEquals(respawnInteractionTimeMessage2, respawnInteractionTimeMessage);
    assertEquals(
        respawnInteractionTimeMessage2.hashCode(), respawnInteractionTimeMessage.hashCode());

    gameService.onRespawnInteractionTimeMessage(respawnInteractionTimeMessage);

    verify(eventBus, Mockito.never()).post(Mockito.any());
  }

  @Test
  void onRespawnInteractionTimeMessage_OnlyOneNeedRequest() {
    game.setInGuiShowPhase(true);
    game.getPlayerDirectionInteraction().add(game.getPlayer(user));

    ArgumentCaptor<AbstractRequestMessage> eventCaptor =
        ArgumentCaptor.forClass(AbstractRequestMessage.class);

    gameService.onRespawnInteractionTimeMessage(new RespawnInteractionTimeMessage(lobbyId));

    verify(eventBus).post(eventCaptor.capture());

    RespawnDirectionInteractionRequest respawnDirectionInteractionRequest =
        (RespawnDirectionInteractionRequest) eventCaptor.getValue();

    assertEquals("Gamer1", respawnDirectionInteractionRequest.getUser().getUsername());
  }

  // -------------------------------------------------------------------------------
  // onValidateCardSelectionRequest => GameCardValidationTest
  // -------------------------------------------------------------------------------

  @Test
  void onNextRoundStartForceRequest() {
    //
    // Bad GameID
    gameService.onNextRoundStartForceRequest(new NextRoundStartForceRequest("LOBY", user));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID, but request not from owner
    gameService.onNextRoundStartForceRequest(new NextRoundStartForceRequest(lobbyId, user2));
    verify(eventBus, Mockito.never()).post(Mockito.any());
    //
    // Valid GameID, request from owner
    gameService.onNextRoundStartForceRequest(new NextRoundStartForceRequest(lobbyId, user));
    //
    ArgumentCaptor<ProgrammingCardMessage> responseCaptor =
        ArgumentCaptor.forClass(ProgrammingCardMessage.class);
    verify(eventBus).post(responseCaptor.capture());
    ProgrammingCardMessage response = responseCaptor.getValue();
    assertEquals(lobbyId, response.getGameId());
    //
    // Valid lobby, without game
    Mockito.reset(eventBus);
    Optional<Game> gameOptional = gameService.getGame(lobbyId);
    assertTrue(gameOptional.isPresent());
    gameManagement.dropGame(lobbyId);
    gameService.onNextRoundStartForceRequest(new NextRoundStartForceRequest(lobbyId, user));
    verify(eventBus, Mockito.never()).post(Mockito.any());
  }
}
