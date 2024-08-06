package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.BasicFloorField;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.message.GameStartMessage;
import de.uol.swp.common.game.message.GuiIsFinishMessage;
import de.uol.swp.common.game.message.ProgrammingCardMessage;
import de.uol.swp.common.game.message.RespawnDirectionInteractionMessage;
import de.uol.swp.common.game.message.RespawnInteractionMessage;
import de.uol.swp.common.game.message.RobotInformationMessage;
import de.uol.swp.common.game.message.RoundFinishMessage;
import de.uol.swp.common.game.message.StopTimerMessage;
import de.uol.swp.common.game.message.UnblockCardInteractionMessage;
import de.uol.swp.common.game.message.WaitForNextRoundMessage;
import de.uol.swp.common.game.message.WinMessage;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.player.PlayerType;
import de.uol.swp.common.game.request.GameDTORequest;
import de.uol.swp.common.game.request.NextRoundStartForceRequest;
import de.uol.swp.common.game.request.NextRoundStartRequest;
import de.uol.swp.common.game.request.ProgrammingCardRequest;
import de.uol.swp.common.game.request.RespawnDirectionInteractionRequest;
import de.uol.swp.common.game.request.RespawnInteractionRequest;
import de.uol.swp.common.game.request.ShutdownRobotRequest;
import de.uol.swp.common.game.request.StartGameRequest;
import de.uol.swp.common.game.request.StopTimerRequest;
import de.uol.swp.common.game.request.UnblockCardRequest;
import de.uol.swp.common.game.request.ValidateCardSelectionRequest;
import de.uol.swp.common.game.response.GameDTOResponse;
import de.uol.swp.common.game.response.UnblockCardResponse;
import de.uol.swp.common.game.response.ValidateCardSelectionResponse;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.lobby.message.ChangedSpectatorModeMessage;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.ServerConfig;
import de.uol.swp.server.game.ai.AiService;
import de.uol.swp.server.game.ai.RandomAi;
import de.uol.swp.server.game.ai.RespawnAi;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.lobby.LobbyService;
import de.uol.swp.server.message.GameLogicFinish;
import de.uol.swp.server.message.GameTimerMessage;
import de.uol.swp.server.message.PlayerLeftLobbyMessage;
import de.uol.swp.server.message.ReplaceUserWithAiMessage;
import de.uol.swp.server.message.RespawnInteractionTimeMessage;
import de.uol.swp.server.message.StartRespawnInteractionInternalMessage;
import de.uol.swp.server.message.UpdateUserStatsMessage;
import de.uol.swp.server.message.WaitOnGuiTimerMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for GameService. General communication service in and out of game classes Acts as a front
 * end for the GameManagement, GameLogic, ...
 */
@SuppressWarnings("UnstableApiUsage")
public class GameService extends AbstractService {

  private static final Logger LOG = LogManager.getLogger(GameService.class);
  private static final int RESPAWN_SELECTION_TIME = 10000;
  private static GameService instance = null;
  private final LobbyService lobbyService;
  private final LobbyManagement lobbyManagement;
  private final GameManagement gameManagement;
  private final GameLogicService gameLogicService;
  private final GameTimerService gameTimerService;
  private final GameRequestAuthenticator gameRequestAuthenticator;
  private final long thresholdForNetworkInteraction;
  private final boolean isInTestmode;
  private final AiService aiService;

  /**
   * Constructor.
   *
   * @param bus the EvenBus used throughout the server
   * @param gameManagement the server-wide GameManagement
   * @param lobbyManagement the server-wide LobbyManagement
   * @param lobbyService the server-wide LobbyService
   */
  @Inject
  public GameService(
      EventBus bus,
      GameManagement gameManagement,
      LobbyManagement lobbyManagement,
      LobbyService lobbyService) {
    this(bus, gameManagement, lobbyManagement, lobbyService, -2);
  }

  /**
   * Constructor.
   *
   * @param bus the EvenBus used throughout the server
   * @param gameManagement the server-wide GameManagement
   * @param lobbyManagement the server-wide LobbyManagement
   * @param lobbyService the server-wide LobbyService
   * @param requestTimeoutThreshold <-1: use ServerConfig, >=-1 for Unit Tests
   */
  @SuppressWarnings("java:S3010")
  public GameService(
      EventBus bus,
      GameManagement gameManagement,
      LobbyManagement lobbyManagement,
      LobbyService lobbyService,
      long requestTimeoutThreshold) {
    super(bus);
    LOG.debug("GameService created: {}{}", this, (instance == null) ? "" : " - DUPLICATION!");
    this.gameManagement = gameManagement;
    this.lobbyManagement = lobbyManagement;
    this.lobbyService = lobbyService;
    if (requestTimeoutThreshold < -1) {
      thresholdForNetworkInteraction = ServerConfig.getInstance().getThreshold();
      this.isInTestmode = false;
    } else {
      // IMPORTANT: to guaranty failure of the GameRequestAuthenticator
      //  a threshold of -1 is needed
      thresholdForNetworkInteraction = requestTimeoutThreshold;
      this.isInTestmode = true;
    }
    instance = this;

    this.gameRequestAuthenticator =
        new GameRequestAuthenticator(this, thresholdForNetworkInteraction);
    this.gameTimerService = new GameTimerService(bus);
    gameLogicService = new GameLogicService(this, bus);
    this.aiService = new AiService(bus);
  }

  /** Return to the existing instance of GameService. */
  @SuppressWarnings("java:S112")
  public static GameService getInstance() {
    if (instance == null) {
      String errmsg =
          "GameService NOT YET created! Injection/Initialization mechanism"
              + "doesn't work correctly.";
      LOG.fatal("{}", errmsg);
      throw new RuntimeException(errmsg);
    }
    return instance;
  }

  /**
   * Handles the StartGameRequest found on the Eventbus.
   *
   * @param request StartGameRequest
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onStartGameRequest(StartGameRequest request) {
    LOG.debug(
        "Received Game start request for lobby {} from user {}",
        request.getLobbyId(),
        request.getUser().getUsername());
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent() && lobby.get().getOwner().hashCode() == request.getUser().hashCode()) {
      gameManagement.createGame(lobbyManagement.getLobby(request.getLobbyId()));
      Optional<Game> game = gameManagement.getGame(request.getLobbyId());
      if (game.isPresent()) {
        LobbyOptions options = lobby.get().getOptions();
        options.setLobbyStatus(LobbyOptions.LobbyStatus.INGAME);
        lobby.get().updateOptions(options);
        post(
            new UpdateLobbyListMessage(
                lobby.get().getLobbyId(),
                lobby.get().getOptions().getLobbyTitle(),
                lobby.get().getPlayerCount(),
                lobby.get().getOptions().getSlot(),
                lobby.get().getLobbyStatus(),
                lobby.get().getOwner().getUsername(),
                lobby.get().getOptions().isActiveLasers(),
                lobby.get().getOptions().isSwitchOffRoboter(),
                lobby.get().getOptions().isWeakDuplicatedActive()));
        lobbyService.sendToAllInLobby(
            request.getLobbyId(), new GameStartMessage(request.getLobbyId()));
        GameDTO gameDTO = (GameDTO) game.get();
        if (request.isWithCommandStarted()) {
          gameDTO.setHasStartWithCommand(true);
        }
        game.get().getPlayers().stream()
            .map(Player::getRobot)
            .forEach(
                robot -> game.get().changeRobotOnRespawnFieldOrder(robot.getPosition(), robot));
      }
    }
  }

  /**
   * Handles GameDTORequest found on the EventBus.
   *
   * @param request GameDTORequest
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onGameDTORequest(GameDTORequest request) {
    Optional<Game> game = gameManagement.getGame(request.getGameId());
    ResponseMessage returnMessage = new GameDTOResponse((GameDTO) game.orElse(null));
    request.getMessageContext().ifPresent(returnMessage::setMessageContext);
    LOG.trace("Sending GameDTO");
    post(returnMessage);
  }

  /**
   * Handles the ProgrammingCardRequest found on the Event Bus.
   *
   * @param request ProgrammingCardRequest
   */
  @Subscribe
  public void onProgrammingCardRequest(ProgrammingCardRequest request) {
    String gameId = request.getLobbyId().replace("Game ", "");
    LOG.trace("Received ProgrammingCardRequest for Game {}", gameId);
    sendProgramingCards(gameId);
  }

  /**
   * Handles the UnblockCardRequest found on the Event Bus.
   *
   * @param request UnblockCardRequest
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onUnblockCardRequest(UnblockCardRequest request) {
    LOG.debug(
        "Received UnblockCardRequest for Game {}, User {}, CardSlot#{}",
        request.getGameId(),
        request.getUsername(),
        request.getToUnblock());
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      GameDTO gameDTO = (GameDTO) gameOptional.get();
      Optional<Player> playerOptional = gameDTO.findPlayerByName(request.getUsername());
      if (playerOptional.isPresent()) {
        Player player = playerOptional.get();
        Robot robot = player.getRobot();
        robot.unblockSlot(request.getToUnblock());

        UnblockCardResponse response =
            new UnblockCardResponse(player.getUser(), request.getGameId(), robot.getBlocked());
        request.getMessageContext().ifPresent(response::setMessageContext);
        post(response);
      }
    }
  }

  /**
   * This method send new Programming Cards to all players in a game.
   *
   * @param user The Name of the Game
   */
  public void sendUnblockCardSlotInteraction(User user, String gameId, int amount) {
    LOG.debug("Sending UnblockCardInteractionMessage to {}", user.getUsername());
    Optional<Game> game = gameManagement.getGame(gameId);
    game.ifPresent(
        value ->
            sendToAllInGame(
                gameId,
                new UnblockCardInteractionMessage(
                    user,
                    value.getGameId(),
                    value.getPlayer(user).getRobot().getBlocked(),
                    amount)));
  }

  /**
   * This method sends new Programming Cards to all players in a game.
   *
   * @param gameId The Name of the Game
   */
  public void sendProgramingCards(String gameId) {
    LOG.debug("Trying to send ProgrammingCards for GAME {}", gameId);
    Optional<Game> game = gameManagement.getGame(gameId);
    if (game.isPresent()) {
      Collection<Player> players =
          game.get().getPlayers().stream()
              .filter(Player::isNotSpectator)
              .collect(Collectors.toList());
      LOG.trace("Players in Game {} are: {}", game.get().getGameId(), players);

      int[] damage = new int[players.size()];
      Map<Player, ProgrammingCard[]> returnCards = new HashMap<>();
      Map<Player, boolean[]> blockedSlots = new HashMap<>();
      int index1 = 0;
      int index2 = 0;

      for (Player player : players) {
        damage[index1] = player.getRobot().getDamage();
        index1++;
      }
      List<ProgrammingCard> cardsAlreadyInUse = new ArrayList<>();
      for (Player player : players) {
        cardsAlreadyInUse.addAll(player.getRobot().getBlockedCards());
      }
      ProgrammingCard[][] cards = game.get().getCardDealer().dealCards(damage, cardsAlreadyInUse);
      for (Player player : players) {
        Robot robot = player.getRobot();
        Arrays.stream(cards[index2]).forEach(card -> card.setOwner(robot.getType()));
        returnCards.put(player, cards[index2]);
        blockedSlots.put(player, robot.getBlocked());
        robot.setHandCards(cards[index2]);
        if (robot.isShutdown()) {
          returnCards.put(player, new ProgrammingCard[0]);
          robot.setHandCards(new ProgrammingCard[0]);
        }
        index2++;
      }

      if (LOG.isTraceEnabled()) {
        returnCards.forEach((k, v) -> LOG.trace("Sending Cards {} {}", k.getName(), v.length));
      }
      ((GameDTO) game.get()).setTimerStopped(false);
      sendToAllInGame(gameId, new ProgrammingCardMessage(returnCards, gameId, blockedSlots));
      calculateAi((GameDTO) game.get(), players, returnCards);
    }
  }

  /**
   * This method is called when the gameTimer is finished. It starts the GameLogic.
   *
   * @param msg the event found on the eventBus
   */
  @Subscribe
  public void onGameTimerMessage(GameTimerMessage msg) {
    String gameId = msg.getLobbyId();
    Optional<Game> game = gameManagement.getGame(gameId);
    if (game.isPresent()) {
      game.get().firstCardFirstRoundMoveCard();
      LOG.debug("Game {}: Time is over, start logic...", gameId);
      gameLogicService.startLogic(game.get());
    }
  }

  /**
   * Handles the ReplaceUserWithAiMessage found on the Eventbus.
   *
   * @param msg the ReplaceUserWithAiMessage found on the Eventbus
   */
  @Subscribe
  public void onReplaceUserWithAiMessage(ReplaceUserWithAiMessage msg) {
    Optional<Lobby> optionalLobby = lobbyManagement.getLobby(msg.getLobbyId());
    if (optionalLobby.isPresent()) {
      Lobby lobby = optionalLobby.get();
      Optional<Game> gameOptional = gameManagement.getGame(lobby.getLobbyId());
      gameOptional.ifPresent(game -> game.replaceUserWithAiPlayer(msg.getUser()));
      ChatMessageMessage chatMsg =
          new ChatMessageMessage(
              new ChatMessageDTO(
                  (UserDTO) msg.getUser(),
                  "Danke für das Spiel ab jetzt spielt eine Ki für mich weiter",
                  lobby.getLobbyId()));
      post(chatMsg);
    }
  }

  /**
   * This method is called when the GameLogic is finished. It sends a new message to all players in
   * a game by use of the step object.
   *
   * @param gameLogicFinish the event found on the eventBus
   */
  @Subscribe
  public void onGameLogicFinish(GameLogicFinish gameLogicFinish) {
    String gameId = gameLogicFinish.getLobby().getLobbyId();
    Optional<Game> gameOptional = gameManagement.getGame(gameId);
    if (gameOptional.isPresent()) {
      Game game = gameOptional.get();
      if (game.getWinner() == null) {
        sendToAllInGame(gameId, new RoundFinishMessage(gameLogicFinish.getStep()));
        game.setInGuiShowPhase(true);
        game.setInFirstRoundOfRespawnProcess(true);
        gameTimerService.waitOnGuiTimer(
            gameLogicFinish.getStep().getSteps().size() * 2L * 1000
                + thresholdForNetworkInteraction,
            gameId);
      }
    }
  }

  /**
   * waits for the GUI after the Factory phases to be done.
   *
   * @param message contains Game of the Respawn
   */
  @Subscribe
  public void onWaitOnGuiTimerMessage(WaitOnGuiTimerMessage message) {
    Optional<Game> optionalGame = gameManagement.getGame(message.getGameId());
    if (optionalGame.isPresent()) {
      LOG.trace("Wait on Gui Timer ends");
      Game game = optionalGame.get();
      if (game.isInGuiShowPhase()) {
        LOG.trace("GameDTO is in GuiShowPhase");
        game.setInGuiShowPhase(false);
        post(new StartRespawnInteractionInternalMessage(game.getGameId()));
      }
    }
  }

  /**
   * Method that starts the Respawn process if a Roboter was destroyed in the last round .
   *
   * @param message StartRespawnMessage, that indicates in which game and which robot must be
   *     respawned
   */
  @Subscribe
  @SuppressWarnings("java:S3776")
  public void onStartRespawnInteractionInternalMessage(
      StartRespawnInteractionInternalMessage message) {
    Optional<Game> optionalGame = gameManagement.getGame(message.getGameId());
    AtomicBoolean startTimer = new AtomicBoolean(false);
    if (optionalGame.isPresent()) {
      Game game = optionalGame.get();
      LOG.trace("Start Respawn process");

      if (game.getRobotOnRoundRespawnQueue().isEmpty() && (!isInTestmode)) {
        lobbyService.sendToAllInLobby(
            game.getGameId(), new WaitForNextRoundMessage(game.getGameId()));
        game.getPlayers()
            .forEach(
                player -> {
                  Robot robot = player.getRobot();
                  lobbyService.sendToAllInLobby(
                      game.getGameId(),
                      new RobotInformationMessage(
                          robot.getType(), robot.getRobotInformation(), game.getGameId()));
                });
      }
      game.getRobotOnRoundRespawnQueue()
          .forEach(
              (field, robots) -> {
                if (game.isInFirstRoundOfRespawnProcess() && !robots.isEmpty()) {
                  LOG.trace("First Respawn");

                  startTimer.set(true);
                  Robot robot = robots.poll();
                  if (game.getPlayer(robot).getPlayerType() == PlayerType.HUMAN_PLAYER) {
                    firstRespawn(robot, game);
                  } else { // is an Ai player
                    startRespawnOnlyDirectionAi((GameDTO) game, robot);
                  }
                }
                if (!robots.isEmpty()) {
                  LOG.trace("Respawn");

                  Robot robot = robots.poll();
                  if (game.getPlayer(robot).getPlayerType() == PlayerType.HUMAN_PLAYER) {
                    respawn(robot, game);
                  } else { // is an Ai player
                    startRespawnAi((GameDTO) game, robot);
                  }
                }
              });
      if (startTimer.get()) {
        gameTimerService.respawnInteractionTimer(
            RESPAWN_SELECTION_TIME + thresholdForNetworkInteraction, game.getGameId());
      }
      if (game.isInFirstRoundOfRespawnProcess()) {
        game.setInFirstRoundOfRespawnProcess(false);
      }
      List<FloorField> toRemove = new ArrayList<>();
      for (FloorField field : game.getRobotOnRoundRespawnQueue().keySet()) {
        if (game.getRobotOnRoundRespawnQueue().get(field).isEmpty()) {
          toRemove.add(field);
        }
      }
      toRemove.forEach(k -> game.getRobotOnRoundRespawnQueue().remove(k));
    }
  }

  /**
   * Respawn process for the first Robot, that would spawn on the Checkpoint.
   *
   * @param robot which robot respawns
   * @param game which game the Respawn takes place
   */
  private void firstRespawn(Robot robot, Game game) {
    robot.setNeedGuiInteraction(true);
    game.getPlayerDirectionInteraction().add(game.getPlayer(robot));
    ServerMessage msg =
        new RespawnDirectionInteractionMessage(
            game.getPlayer(robot).getUser(),
            getFloorFieldAsIntArray(game, robot),
            robot.getDirection(),
            RESPAWN_SELECTION_TIME,
            game.getGameId());
    if (isInTestmode) {
      post(msg);
    } else {
      sendToAllInGame(game.getGameId(), msg);
    }
  }

  /**
   * Respawn process for all robots except the first.
   *
   * @param robot Robot that respawns
   * @param game game of the Respawn process
   */
  private void respawn(Robot robot, Game game) {
    robot.setNeedGuiInteraction(true);
    game.getPlayerDirectionInteraction().add(game.getPlayer(robot));
    game.getPlayerFloorFieldInteraction().add(game.getPlayer(robot));
    ServerMessage msg =
        new RespawnInteractionMessage(
            game.getPlayer(robot).getUser(),
            getFloorFieldAsIntArray(game, robot),
            robot.getDirection(),
            RESPAWN_SELECTION_TIME,
            game.getGameId());
    if (isInTestmode) {
      post(msg);
    } else {
      sendToAllInGame(game.getGameId(), msg);
    }
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void startRespawnAi(GameDTO gameDTO, Robot robot) {
    aiService.startAi(
        gameDTO, gameDTO.getPlayer(robot), new RespawnAi(getFloorFieldAsIntArray(gameDTO, robot)));
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void startRespawnOnlyDirectionAi(GameDTO gameDTO, Robot robot) {
    aiService.startAi(gameDTO, gameDTO.getPlayer(robot), new RespawnAi());
  }

  /**
   * Method to tell the Client what it has to show after the Gui is finished.
   *
   * @param message After all changes from all factory phases are done
   */
  @Subscribe
  public void onGuiIsFinishMessage(GuiIsFinishMessage message) {
    Optional<Game> gameOptional = gameManagement.getGame(message.getGameId());
    if (gameOptional.isPresent()) {
      Game game = gameOptional.get();
      Player player = game.getPlayer(message.getUser());
      if (game.isInGuiShowPhase()) {
        if (game.getGuiIsNotAnymoreInShowPhase().contains(player)) {
          LOG.info("Player {} is already finished with the GUI", player.getName());
          return;
        }
        game.addGuiIsNotAnymoreInShowPhase(player);
        if (game.getPlayers().size() == game.getGuiIsNotAnymoreInShowPhase().size()) {
          game.deleteGuiIsNotAnymoreInShowPhase();
          post(new WaitOnGuiTimerMessage(game.getGameId()));
        }
      }
    }
  }

  /**
   * Method, that will set the Robot on the correct field, that the Player chose.
   *
   * @param request contains Robot floor-field and Game, so the spawn is correct
   */
  @Subscribe
  @SuppressWarnings("java:S3776")
  public void onRespawnInteractionRequest(RespawnInteractionRequest request) {
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      Game game = gameOptional.get();
      Player player = game.getPlayer(request.getUser());
      game.getPlayerFloorFieldInteraction().remove(player);
      Robot robot = player.getRobot();
      if (robot.isNeedGuiInteraction()) {
        robot.setNeedGuiInteraction(false);
        FloorField[] floorField =
            getPartOfField(game, robot.getPosition().getX(), robot.getPosition().getY());
        if (request.getNewPosition() != -1 && floorField[request.getNewPosition()] != null) {
          if (floorField[request.getNewPosition()]
              .getBasicFloorField()
              .getOperations()
              .containsKey(-1)) {
            try {
              destroy(robot, game); // Player becomes spectator
            } catch (NoSuchElementException e) {
              throw new IllegalArgumentException("Player not found");
            }
          } else {
            robot.setPosition(floorField[request.getNewPosition()]);
          }
        } else {
          for (FloorField field : floorField) {
            if (field != null) {
              if (field.getBasicFloorField().getOperations().containsKey(-1)) {
                try {
                  destroy(robot, game); // Player becomes spectator
                } catch (NoSuchElementException e) {
                  throw new IllegalArgumentException("Player not found");
                }
              } else {
                robot.setPosition(field);
              }

              break;
            }
          }
        }
        if (request.getDirection() != null) {
          robot.setDirection(request.getDirection());
        } else {
          Random random = new Random(System.currentTimeMillis());
          robot.setDirection(Direction.get(random.nextInt(4)));
        }
        sendToAllInGame(
            game.getGameId(),
            new RobotInformationMessage(
                robot.getType(), robot.getRobotInformation(), game.getGameId()));

        if (game.getPlayers().stream()
            .filter(Player::isNotSpectator)
            .map(Player::getRobot)
            .noneMatch(Robot::isNeedGuiInteraction)) {
          post(new StartRespawnInteractionInternalMessage(game.getGameId()));
        }
      }
    }
  }

  private void destroy(Robot robot, Game game) {
    robot.destroy();
    sendRobotDeadChatMessage(game.getGameId(), game.getPlayer(robot));
    checkWinnerDeath(game);
  }

  private void checkWinnerDeath(Game game) {
    List<Player> alive =
        game.getPlayers().stream().filter(Player::isNotSpectator).collect(Collectors.toList());
    if (alive.size() == 1) {
      endGame(alive.get(0), game);
    } else if (alive.isEmpty()) {
      throw new IllegalStateException("All Robots are dead, but the game has not terminated.");
    }
  }

  private void endGame(Player player, Game game) {
    createUserStatistics(game);
    game.setWinner(player);
    ServerMessage serverMessage = new WinMessage(game, player);
    sendToAllInGame(game.getLobby().getLobbyId(), serverMessage);
    game.getLobby().setLobbyStatus(LobbyStatus.WAITING);

    getGameManagement().dropGame(game.getGameId());
    getGameTimerService().dropAllTimer(game.getGameId());
  }

  private void createUserStatistics(Game game) {
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
      post(new UpdateUserStatsMessage(game.getPlayer(robot).getUser(), userStatistic));
    }
  }

  private UserStatistic createUserStatistic(int placement) {
    return new UserStatistic(placement, LocalDateTime.now());
  }

  /**
   * Method that lets the robot face the desired Direction after Respawn.
   *
   * @param request contains Robot direction and the Game, so the Respawn Direction of the Robot is
   *     correct
   */
  @Subscribe
  public void onRespawnDirectionInteractionRequest(RespawnDirectionInteractionRequest request) {
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      Game game = gameOptional.get();
      Player player = game.getPlayer(request.getUser());
      game.getPlayerDirectionInteraction().remove(player);
      Robot robot = player.getRobot();
      if (robot.isNeedGuiInteraction()) {
        robot.setNeedGuiInteraction(false);
        if (request.getDirection() != null) {
          robot.setDirection(request.getDirection());
        } else {
          Random random = new Random(System.currentTimeMillis());
          robot.setDirection(Direction.get(random.nextInt(4)));
        }
        sendToAllInGame(
            game.getGameId(),
            new RobotInformationMessage(
                robot.getType(), robot.getRobotInformation(), game.getGameId()));

        if (game.getPlayers().stream()
            .filter(Player::isNotSpectator)
            .map(Player::getRobot)
            .noneMatch(Robot::isNeedGuiInteraction)) {
          post(new StartRespawnInteractionInternalMessage(game.getGameId()));
        }
      }
    }
  }

  /**
   * Checks if all Players have selected all necessary Points to respawn correctly after the
   * Respawntimer runs out. If anything is not selected random values are set.
   *
   * @param message contains game in which the Respawn will take place
   */
  @Subscribe
  public void onRespawnInteractionTimeMessage(RespawnInteractionTimeMessage message) {
    Optional<Game> gameOptional = gameManagement.getGame(message.getGameId());
    if (gameOptional.isPresent()) {
      Game game = gameOptional.get();
      for (Player player :
          game.getPlayers().stream().filter(Player::isNotSpectator).collect(Collectors.toList())) {
        if (game.getPlayerDirectionInteraction().contains(player)) {
          post(new RespawnDirectionInteractionRequest(game.getGameId(), player.getUser(), null));
        } else if (game.getPlayerFloorFieldInteraction().contains(player)) {
          post(new RespawnInteractionRequest(game.getGameId(), player.getUser(), -1, null));
        }
      }
    }
  }

  /**
   * Handles the ValidateCardSelectionRequest when it's found on the eventbus.
   *
   * @param request the event found on the eventBus
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onValidateCardSelectionRequest(ValidateCardSelectionRequest request) {
    String gameId = request.getGameId();
    String userName = request.getUsername();
    LOG.debug("Received ValidateCardSelectionRequest for Game {} from user {}", gameId, userName);
    if (gameRequestAuthenticator.isRequestValid(userName, gameId, request.getTimeStamp())) {

      Optional<Game> gameOptional = gameManagement.getGame(gameId);
      if (gameOptional.isEmpty()) {
        return;
      }
      GameDTO gameDTO = (GameDTO) gameOptional.get();

      Optional<Player> playerOptional = gameDTO.findPlayerByName(userName);
      if (playerOptional.isEmpty()) {
        return;
      }
      Robot robot = playerOptional.get().getRobot();

      ProgrammingCard[] requestSelectedCards = request.getSelectedCards();

      boolean fitsHandAndBlocked = robot.updateSelectedCards(requestSelectedCards);
      ValidateCardSelectionResponse response =
          new ValidateCardSelectionResponse(
              request.getRequestId(),
              gameId,
              userName,
              fitsHandAndBlocked ? robot.getSelectedCards() : requestSelectedCards,
              fitsHandAndBlocked);
      request.getMessageContext().ifPresent(response::setMessageContext);
      post(response);
    }
  }

  /**
   * Handles the StopTimerRequest when it's found on the eventbus. It sets the timerStopped boolean
   * of the GameDTO to true and sends a StopTimerMessage to everyone in the game.
   *
   * @param request StopTimerRequest
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  @Subscribe
  public void onStopTimerRequest(StopTimerRequest request) {
    LOG.debug(
        "Received StopTimerRequest for Game {} from user {}",
        request.getGameId(),
        request.getUsername());
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      GameDTO gameDTO = (GameDTO) gameOptional.get();
      gameDTO.setTimerStopped(true);
      LOG.trace("Timer of Game {} has been stopped", gameDTO.getGameId());
      sendToAllInGame(
          gameDTO.getGameId(), new StopTimerMessage(request.getUsername(), gameDTO.getGameId()));
    }
  }

  /**
   * The request is called from the user who wants to leave a lobby. the Method removes him from the
   * game
   */
  @Subscribe
  public void onPlayerLeftLobbyMessage(PlayerLeftLobbyMessage playerLeftLobbyMessage) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Received new PlayerLeftLobbyMessage message from user {}",
          playerLeftLobbyMessage.getUser().getUsername());
    }
    Optional<Game> game = gameManagement.getGame(playerLeftLobbyMessage.getLobby().getLobbyId());
    game.ifPresent(value -> value.leaveUser(playerLeftLobbyMessage.getUser()));
  }

  /**
   * The request is called from the user who wants to start a new round. When all players are ready,
   * the method starts a new round.
   */
  @Subscribe
  public void onNextRoundStartRequest(NextRoundStartRequest request) {
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      GameDTO game = (GameDTO) gameOptional.get();
      Player player = game.getPlayer(request.getUser());
      if (game.getRequestNextRoundPlayer().contains(player)) {
        LOG.info("Player {} has already requested a next round", player.getName());
        return;
      }
      game.addRequestNextRoundPlayer(player);
      if (game.getPlayers().stream().filter(Player::isNotSpectator).count()
          <= game.getRequestNextRoundPlayer().size()) {
        LOG.debug("start round from normal user request");
        startNextRound(game);
      }
    }
  }

  /**
   * This request is called from the game owner who wants to force a new round. It ignores all other
   * player and start a new round.
   */
  @Subscribe
  public void onNextRoundStartForceRequest(NextRoundStartForceRequest request) {
    Optional<Game> gameOptional = gameManagement.getGame(request.getGameId());
    if (gameOptional.isPresent()) {
      GameDTO game = (GameDTO) gameOptional.get();
      if (game.getLobby().getOwner().equals(request.getUser())) {
        LOG.debug("start Round from force user request");
        startNextRound(game);
      }
    }
  }

  private void startNextRound(GameDTO game) {
    game.deleteRequestNextRoundPlayer();
    sendProgramingCards(game.getGameId());
    gameTimerService.gameTimer(
        game.getLobby().getOptions().getTurnLimit() + thresholdForNetworkInteraction,
        game.getGameId());
  }

  /**
   * Searches for the game with the given gameId. Acts as a front end for the GameManagement
   *
   * @param gameId String containing the id of the game to search for
   * @return either empty Optional or Optional containing the game
   * @see Optional
   */
  public Optional<Game> getGame(String gameId) {
    return gameManagement.getGame(gameId);
  }

  /**
   * Sends a message to all players in the game.
   *
   * @param gameId the id of the game
   * @param message The message to send to all users in the game
   */
  public void sendToAllInGame(String gameId, ServerMessage message) {
    lobbyService.sendToAllInLobby(gameId, message);
  }

  /**
   * Handles the ShutdownRobotRequest found on the eventbus.
   *
   * @param request the ShutdownRobotRequest
   */
  @Subscribe
  public void onShutdownRobotRequest(ShutdownRobotRequest request) {
    Optional<Lobby> optionalLobby = lobbyManagement.getLobby(request.getGameId());
    if (optionalLobby.isEmpty()) {
      LOG.error("Lobby not Found");
      return;
    }
    Optional<Game> gameOptional = gameManagement.getGame(optionalLobby.get().getLobbyId());
    if (gameOptional.isPresent()) {
      gameOptional
          .get()
          .getPlayers()
          .forEach(
              player -> {
                if (player.getUser().equals(request.getUser())) {
                  ChatMessageDTO msg =
                      new ChatMessageDTO(
                          (UserDTO) request.getUser(),
                          "Ich habe meinen Roboter deaktiviert "
                              + "um meine Schadenspunkte zu entfernen",
                          gameOptional.get().getLobby().getLobbyId());
                  lobbyService.sendToAllInLobby(request.getGameId(), new ChatMessageMessage(msg));
                  player.getRobot().setIsNeedToShutdown(true);
                }
              });
    } else {
      LOG.error("GameDTO is empty at reaction on Shutdownrequest");
    }
  }

  /**
   * Sends a chat message to all players that the named player lost a live or became a spectator.
   *
   * @param gameId the ID of the game
   * @param player the player whose robot died
   */
  public void sendRobotDeadChatMessage(String gameId, Player player) {
    String msg =
        "Der Roboter des Spielers "
            + player.getName()
            + " ist gestorben, er hat nun noch "
            + player.getRobot().getLives()
            + " Leben!";
    if (!player.isNotSpectator()) {
      msg += " Der Spieler ist nun Zuschauer!";
    }
    LOG.debug(msg);
    sendToAllInGame(gameId, new ChatMessageMessage(new ChatMessageDTO(null, msg, gameId)));
  }

  /**
   * Sends a chat message to all players from the Ai.
   *
   * @param player the player sends the message
   * @param gameId the ID of the game
   * @param testMode boolean whether test mode is true or not
   */
  public void sendAiMessage(String gameId, Player player, boolean testMode) {
    int randomValue = 0;
    for (char value : player.getUser().getUUID().toCharArray()) {
      randomValue = randomValue + value;
    }

    Random random = new Random(System.currentTimeMillis() + randomValue);

    String filePath = "server/src/main/resources/aiMessages.txt";

    try {
      Path path = Paths.get(filePath);
      List<String> lines = Files.readAllLines(path);

      int lineNumber = random.nextInt(lines.size() - 1);
      if (!testMode) {
        sendToAllInGame(
            gameId,
            new ChatMessageMessage(
                new ChatMessageDTO((UserDTO) player.getUser(), lines.get(lineNumber), gameId)));
      }
    } catch (IOException e) {
      LOG.error("Can't read the input: {}", e.getMessage());
    }
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private void calculateAi(
      GameDTO gameDTO, Collection<Player> players, Map<Player, ProgrammingCard[]> returnCards) {
    players.stream()
        .filter(player -> player.getPlayerType() != PlayerType.HUMAN_PLAYER)
        .forEach(
            player -> aiService.startAi(gameDTO, player, new RandomAi(returnCards.get(player))));
  }

  private FloorField[] getPartOfField(Game game, int y, int x) {
    x++;
    y++;
    List<FloorField> returnString = new ArrayList<>();
    for (int i = -1; i < 2; i++) {
      for (int j = -1; j < 2; j++) {
        if (!isValidPosition(x, y, i, j)) {
          returnString.add(new FloorField(null, new BasicFloorField("-1", null, null), -2, -2));
          continue;
        }
        FloorField field = game.getFloorPlan().getFloorFields(x + i, y + j);
        if (!isPlayerRobotOccupyingField(game, field)) {
          returnString.add(field);
        } else {
          returnString.add(new FloorField(null, new BasicFloorField("-1", null, null), -2, -2));
        }
      }
    }
    return returnString.toArray(new FloorField[0]);
  }

  private Integer[][] getFloorFieldAsIntArray(Game game, Robot robot) {
    FloorField[] floorFields =
        getPartOfField(game, robot.getPosition().getX(), robot.getPosition().getY());
    Integer[][] returnArray = new Integer[floorFields.length][2];
    for (int i = 0; i < floorFields.length; i++) {
      if (!floorFields[i].getBasicFloorField().getId().equals("-1")) {
        returnArray[i][0] = Integer.valueOf(floorFields[i].getBasicFloorField().getId());
        returnArray[i][1] = floorFields[i].getDirection().getNumber();
      } else {
        returnArray[i][0] = -1;
        returnArray[i][1] = 0;
      }
    }
    return returnArray;
  }

  private boolean isValidPosition(int x, int y, int i, int j) {
    return i + x >= 0 && j + y >= 0;
  }

  private boolean isPlayerRobotOccupyingField(Game game, FloorField field) {
    return game.getPlayers().stream()
        .filter(Player::isNotSpectator)
        .map(Player::getRobot)
        .anyMatch(robot -> robot.getPosition().equals(field));
  }

  public GameLogicService getGameLogicService() {
    return gameLogicService;
  }

  public GameManagement getGameManagement() {
    return gameManagement;
  }

  public GameTimerService getGameTimerService() {
    return gameTimerService;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void sendChangeSpectatorMode(Game game, Robot robot) {
    post(
        new ChangedSpectatorModeMessage(
            game.getGameId(),
            (UserDTO) game.getPlayer(robot).getUser(),
            true,
            game.getLobby().getOwner()));
  }
}
