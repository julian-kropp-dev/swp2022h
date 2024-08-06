package de.uol.swp.common.game;

import de.uol.swp.common.game.card.CardDealer;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.user.User;
import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Interface to unify game objects.
 *
 * <p>This is an Interface to allow for multiple types of game objects since it is possible that not
 * every client has to have every information of the game.
 *
 * @see de.uol.swp.common.game.dto.GameDTO
 */
public interface Game extends Serializable {

  /**
   * Getter for the game's name. Note: The game name should be the same as the lobby name because a
   * lobby is associated with exactly one game
   *
   * @return A String containing the name of the game.
   */
  String getGameId();

  /**
   * Removes a user from the game.
   *
   * @param user The user to remove from the game
   */
  void leaveUser(User user);

  /**
   * Getter to get a Set of the player who are part of the game.
   *
   * @return set of users
   */
  Set<Player> getPlayers();

  /**
   * Returns the lobby associated with the current instance of the class.
   *
   * @return the lobby associated with the current instance of the class
   */
  Lobby getLobby();

  /**
   * Sets the lobby for the current instance of the class.
   *
   * @param lobby the lobby to be set
   */
  void setLobby(Lobby lobby);

  /**
   * Getter for a Player of a User in this Game.
   *
   * @param user The User
   * @return Player of the User
   */
  Player getPlayer(User user) throws NoSuchElementException;

  /**
   * Getter for a Player of a Robot in this Game.
   *
   * @param robot The Robot
   * @return Player of the Robot
   */
  Player getPlayer(Robot robot) throws NoSuchElementException;

  /**
   * Getter for an Optional Player of a User in this Game by username.
   *
   * @param userName The name of the user of the player
   * @return Optional Player if player of this username exists in this game
   */
  Optional<Player> findPlayerByName(String userName);

  /**
   * Getter for the CardDealer.
   *
   * @return CardDealer
   */
  CardDealer getCardDealer();

  /** Getter for the FloorPlan of a Game. */
  FloorPlan getFloorPlan();

  /** Getter for the player that has win a game. */
  Player getWinner();

  /** Setter for the player that has win a game. */
  void setWinner(Player player);

  /** Getter fot the queue of the respawn process of the robot, that died. */
  Map<FloorField, Deque<Robot>> getRobotOnRoundRespawnQueue();

  /** Setter fot the queue of the respawn process of the robot, that died. */
  void setRobotOnRoundRespawnQueue(Map<FloorField, Deque<Robot>> list);

  /** Get the respawn order of the checkpoint. */
  Map<FloorField, List<Robot>> getRespawnFieldOrder();

  /** Changes the respawn order of the checkpoint. */
  void changeRobotOnRespawnFieldOrder(FloorField field, Robot robot);

  /** Get the state of the GUI phase from the Client. */
  boolean isInGuiShowPhase();

  /** Sets the GUI phase. */
  void setInGuiShowPhase(boolean inGuiShowPhase);

  /** Get the player, who wants to start a next round. */
  List<Player> getRequestNextRoundPlayer();

  /** Add the player, who requests to start the next round. */
  void addRequestNextRoundPlayer(Player player);

  /** Delete all player, who wants to start the next round. */
  void deleteGuiIsNotAnymoreInShowPhase();

  /** Get all players, who wants to start the next round. */
  List<Player> getGuiIsNotAnymoreInShowPhase();

  /** Add a players, who wants to start the next round. */
  void addGuiIsNotAnymoreInShowPhase(Player player);

  /** Delete the player, who requested to start the next round. */
  void deleteRequestNextRoundPlayer();

  /** First respawn of this round. */
  boolean isInFirstRoundOfRespawnProcess();

  /** Sets value, if it is the first respawn round. */
  void setInFirstRoundOfRespawnProcess(boolean inFirstRoundOfRespawnProcess);

  /** All players, who need a Direction to respawn. */
  List<Player> getPlayerDirectionInteraction();

  /** All players, who need a Floor field to respawn. */
  List<Player> getPlayerFloorFieldInteraction();

  /**
   * Is the Game in the first round of the hole game.
   *
   * @return a boolean value
   */
  boolean isInFirstRoundOfTheGame();

  /**
   * Set the Game in the first round of the hole game.
   *
   * @param inFirstRoundOfTheGame a boolean value
   */
  void setInFirstRoundOfTheGame(boolean inFirstRoundOfTheGame);

  /** Ensures that the first Card in the first Round is correctly picked by the server. */
  void firstCardFirstRoundMoveCard();

  /**
   * Removes the given User from the game and adds a new Ai player with the leavings Users robots
   * insted.
   *
   * @param user the User to replace by an Ai User
   */
  void replaceUserWithAiPlayer(User user);

  boolean isHasStartWithCommand();

  void setHasStartWithCommand(boolean hasStartWithCommand);
}
