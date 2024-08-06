package de.uol.swp.common.game.dto;

import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.card.CardDealer;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.card.ProgrammingCardManager;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.player.PlayerType;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Object to transfer the information of a game.
 *
 * <p>This object is used to communicate the current state of game between the server and clients.
 * It contains information about player position, life points, robot movement...
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class GameDTO implements Game, Serializable {

  private static final long serialVersionUID = -8497600142086005098L;
  private static final Logger LOG = LogManager.getLogger(GameDTO.class);
  private final String gameId;
  private final Set<Player> players = new HashSet<>();
  private final transient CardDealer dealer;
  private final FloorPlan floorPlan;
  private final List<Player> requestNextRoundPlayer = new ArrayList<>();
  private final Map<FloorField, List<Robot>> robotOnRespawnFieldOrder = new ConcurrentHashMap<>();
  private final Map<FloorField, Deque<Robot>> robotOnRoundRespawnQueue = new ConcurrentHashMap<>();
  private final List<Player> guiIsNotAnymoreInShowPhase = new ArrayList<>();
  private final List<Player> playerDirectionInteraction = new ArrayList<>();
  private final List<Player> playerFloorFieldInteraction = new ArrayList<>();
  private final Deque<String> stringAiDeque;
  private Lobby lobby;
  private boolean timerStopped = false;
  private Player winner;
  private boolean inGuiShowPhase;
  private boolean isInFirstRoundOfRespawnProcess = true;
  private boolean isInFirstRoundOfTheGame = true;
  private boolean firstRound = true;
  private boolean hasStartWithCommand;

  /**
   * Constructor. The FloorPlan is chosen from the given lobby.
   *
   * @param lobby The Lobby of the Game
   */
  public GameDTO(Lobby lobby) {
    this(lobby, new FloorPlan(lobby.getOptions().getFloorPlanAsString()));
  }

  /**
   * Constructor.
   *
   * @param lobby The Lobby of the Game
   * @param floorPlan The chosen FloorPlan for the Game
   */
  public GameDTO(Lobby lobby, FloorPlan floorPlan) {
    this.lobby = lobby;
    this.gameId = lobby.getLobbyId();
    this.dealer =
        new CardDealer(
            ProgrammingCardManager.getProgramingCardSetMove(),
            ProgrammingCardManager.getProgramingCardSetTurn());
    this.floorPlan = floorPlan;
    List<User> sorted = new ArrayList<>(lobby.getUsers());
    Deque<Robots> freeRobots = new ArrayDeque<>(List.of(Robots.values()));
    Collections.sort(sorted);
    for (User user : sorted) {
      if (lobby.getSpectators().contains(user)) {
        players.add(new Player(PlayerType.SPECTATOR, new Robot(Robots.DUMMY, null), user));
        continue;
      }
      Optional<Robots> optionalRobots = lobby.getRobotStyle(user);
      if (optionalRobots.isPresent()) {
        Robot robot = new Robot(optionalRobots.get(), floorPlan.getStart());
        freeRobots.remove(optionalRobots.get());
        players.add(new Player(PlayerType.HUMAN_PLAYER, robot, user));
      } else {
        LOG.error(
            "At least one player does not have a robot style. "
                + "This error occurred while creating Game {}",
            lobby.getLobbyId());
        throw new IllegalStateException("At least one player does not have a robot style");
      }
    }
    List<String> aiNames =
        new ArrayList<>(
            List.of(
                "Knallotron",
                "Nonsensinator",
                "Albernator",
                "Tüddelü-Maschinchen",
                "TüftelDepp",
                "Kizarr",
                "LogikLoser",
                "Seidenschnabel"));
    Collections.shuffle(aiNames);
    stringAiDeque = new ArrayDeque<>(aiNames);
    for (int i = 0; i < lobby.getOptions().getAiPlayerCount(); i++) {
      User aiUser =
          new UserDTO(
              UUID.randomUUID().toString(),
              "[KI] " + stringAiDeque.pop(),
              "",
              "ki@ki.ki",
              new UserData(1));
      Robot robot = new Robot(freeRobots.pop(), floorPlan.getStart());
      Player player =
          new Player(PlayerType.fromInt(lobby.getOptions().getAiDifficulty()[i]), robot, aiUser);
      players.add(player);
    }
    LOG.debug("Create GameDTO {} with {} players", lobby.getLobbyId(), players.size());
  }

  /**
   * Getter for GameName.
   *
   * @return GameName.
   */
  @Override
  public String getGameId() {
    return gameId;
  }

  /**
   * Removes a User from the game.
   *
   * @param user The user to remove from the game
   */
  @Override
  public void leaveUser(User user) {
    try {
      players.remove(this.getPlayer(user));
    } catch (NoSuchElementException e) {
      LOG.error("Player of User {} is not in Game {}", user.getUsername(), gameId);
      throw new IllegalStateException("Player is not in game");
    }
    lobby.leaveUser(user);
  }

  /**
   * Getter to get a Set of the player who are part of the game.
   *
   * @return set of users
   */
  @Override
  public Set<Player> getPlayers() {
    return players.stream().filter(Player::isNotSpectator).collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Lobby getLobby() {
    return lobby;
  }

  @Override
  public void setLobby(Lobby lobby) {
    this.lobby = lobby;
  }

  @Override
  public Player getPlayer(User user) throws NoSuchElementException {
    for (Player player : players) {
      if (player.getUser().equals(user)) {
        return player;
      }
    }
    LOG.error("Player for User {} in Game {} does not exists.", user.getUsername(), gameId);
    throw new NoSuchElementException("Player for User does not exists.");
  }

  @Override
  public Player getPlayer(Robot robot) throws NoSuchElementException {
    for (Player player : players) {
      if (player.getRobot().equals(robot)) {
        return player;
      }
    }
    LOG.error("Player for Robot {} in Game {} does not exists.", robot.getType(), gameId);
    throw new NoSuchElementException("Player for User does not exists.");
  }

  @Override
  public Optional<Player> findPlayerByName(String userName) {
    for (Player player : players) {
      if (player.getName().equals(userName)) {
        return Optional.of(player);
      }
    }
    LOG.error("Player for User {} in Game {} not found.", userName, gameId);
    return Optional.empty();
  }

  /**
   * Getter for the winner.
   *
   * @return the player, that has won
   */
  @Override
  public Player getWinner() {
    return winner;
  }

  /**
   * Setter for the winner.
   *
   * @param player that has won
   */
  @Override
  public void setWinner(Player player) {
    this.winner = player;
  }

  /**
   * Returns the CardDealer object used in the game.
   *
   * @return the CardDealer object used in the game
   */
  @Override
  public CardDealer getCardDealer() {
    return dealer;
  }

  /**
   * Returns the FloorPlan object representing the layout of the game board.
   *
   * @return the FloorPlan object representing the layout of the game board
   */
  public FloorPlan getFloorPlan() {
    return floorPlan;
  }

  public boolean isTimerStopped() {
    return timerStopped;
  }

  public void setTimerStopped(boolean timerStopped) {
    this.timerStopped = timerStopped;
  }

  /** Get the player, who wants to start a next round. */
  public List<Player> getRequestNextRoundPlayer() {
    return requestNextRoundPlayer;
  }

  /** Add the player, who wants to start a next round. */
  public void addRequestNextRoundPlayer(Player player) {
    requestNextRoundPlayer.add(player);
  }

  public void deleteGuiIsNotAnymoreInShowPhase() {
    guiIsNotAnymoreInShowPhase.clear();
  }

  public List<Player> getGuiIsNotAnymoreInShowPhase() {
    return guiIsNotAnymoreInShowPhase;
  }

  public void addGuiIsNotAnymoreInShowPhase(Player player) {
    guiIsNotAnymoreInShowPhase.add(player);
  }

  /**
   * Ensures that the firstCard in the first Round is a Move Card unsets the Flag for the first
   * Round after execution. For use in Server only
   */
  public void firstCardFirstRoundMoveCard() {
    if (firstRound) {
      players.forEach(
          t -> {
            ProgrammingCard[] robotCards = t.getRobot().getSelectedCards();
            if (robotCards[0] != null && !robotCards[0].isMoveCard()) {
              ProgrammingCard tmp = robotCards[0];
              for (int i = 1; i < robotCards.length; i++) {
                if (robotCards[i].isMoveCard()) {
                  robotCards[0] = robotCards[i];
                  robotCards[i] = tmp;
                }
              }
            }
          });
    }
    firstRound = false;
  }

  @Override
  public void replaceUserWithAiPlayer(User user) {
    Robot usersRobot = getPlayer(user).getRobot();
    leaveUser(user);
    User kiUser =
        new UserDTO(
            UUID.randomUUID().toString(),
            "[KI] " + stringAiDeque.pop(),
            "",
            "ki@ki.ki",
            new UserData(1));
    Random random = new Random(System.currentTimeMillis());
    int aiLevel = random.nextInt(4) + 1;
    players.add(new Player(PlayerType.fromInt(aiLevel), usersRobot, kiUser));
    getLobby().getOptions().setAiPlayerCount(getLobby().getOptions().getAiPlayerCount() + 1);
  }

  /** Delete all player, who wants to start a next round. */
  public void deleteRequestNextRoundPlayer() {
    requestNextRoundPlayer.clear();
  }

  public Map<FloorField, List<Robot>> getRespawnFieldOrder() {
    return robotOnRespawnFieldOrder;
  }

  /**
   * Changes the order of the robots on a respawn field.
   *
   * @param field the FloorField representing the respawn field
   * @param robot the Robot to change the order for
   */
  public void changeRobotOnRespawnFieldOrder(FloorField field, Robot robot) {
    if (!robotOnRespawnFieldOrder.containsKey(field)
        || (robotOnRespawnFieldOrder.containsKey(field)
            && !robotOnRespawnFieldOrder.get(field).contains(robot))) {
      for (List<Robot> robots : robotOnRespawnFieldOrder.values()) {
        if (robots.contains(robot)) {
          robots.remove(robot);
          break;
        }
      }
      List<Robot> robotList = robotOnRespawnFieldOrder.getOrDefault(field, new LinkedList<>());
      robotList.add(robot);
      robotOnRespawnFieldOrder.put(field, robotList);
    }
    for (Entry<FloorField, List<Robot>> entrySet : robotOnRespawnFieldOrder.entrySet()) {
      if (entrySet.getValue().isEmpty()) {
        robotOnRespawnFieldOrder.remove(entrySet.getKey());
        break;
      }
    }
  }

  public Map<FloorField, Deque<Robot>> getRobotOnRoundRespawnQueue() {
    return robotOnRoundRespawnQueue;
  }

  public void setRobotOnRoundRespawnQueue(Map<FloorField, Deque<Robot>> list) {
    robotOnRoundRespawnQueue.clear();
    robotOnRoundRespawnQueue.putAll(list);
  }

  public boolean isInGuiShowPhase() {
    return inGuiShowPhase;
  }

  public void setInGuiShowPhase(boolean inGuiShowPhase) {
    this.inGuiShowPhase = inGuiShowPhase;
  }

  public boolean isInFirstRoundOfRespawnProcess() {
    return isInFirstRoundOfRespawnProcess;
  }

  public void setInFirstRoundOfRespawnProcess(boolean inFirstRoundOfRespawnProcess) {
    isInFirstRoundOfRespawnProcess = inFirstRoundOfRespawnProcess;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GameDTO gameDTO = (GameDTO) o;
    return gameId.equals(gameDTO.gameId);
  }

  public boolean isInFirstRoundOfTheGame() {
    return isInFirstRoundOfTheGame;
  }

  public void setInFirstRoundOfTheGame(boolean inFirstRoundOfTheGame) {
    isInFirstRoundOfTheGame = inFirstRoundOfTheGame;
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId);
  }

  @Override
  public List<Player> getPlayerDirectionInteraction() {
    return playerDirectionInteraction;
  }

  @Override
  public List<Player> getPlayerFloorFieldInteraction() {
    return playerFloorFieldInteraction;
  }

  public boolean isHasStartWithCommand() {
    return hasStartWithCommand;
  }

  public void setHasStartWithCommand(boolean hasStartWithCommand) {
    this.hasStartWithCommand = hasStartWithCommand;
  }
}
