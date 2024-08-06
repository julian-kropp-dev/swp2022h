package de.uol.swp.common.lobby;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.user.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface to unify lobby objects.
 *
 * <p>This is an Interface to allow for multiple types of lobby objects since it is possible that
 * not every client has to have every information of the lobby.
 *
 * @see de.uol.swp.common.lobby.dto.LobbyDTO
 */
public interface Lobby extends Serializable {

  /**
   * Getter for the lobby's name.
   *
   * @return A String containing the name of the lobby
   */
  String getLobbyId();

  /**
   * Changes the owner of the lobby.
   *
   * @param user The user who should be the new owner
   */
  void updateOwner(User user);

  /**
   * Getter for the current owner of the lobby.
   *
   * @return A User object containing the owner of the lobby
   */
  User getOwner();

  /**
   * Adds a new user to the lobby.
   *
   * @param user The new user to add to the lobby
   */
  void joinUser(User user);

  /**
   * Removes a user from the lobby.
   *
   * @param user The user to remove from the lobby
   */
  void leaveUser(User user);

  /**
   * Adds a new spectator to the lobby.
   *
   * @param user The new spectator to add to the lobby
   */
  void joinSpectator(User user);

  /**
   * Removes a spectator from the lobby.
   *
   * @param user The spectator to remove from the lobby
   */
  void leaveSpectator(User user);

  /**
   * Getter for all users in the lobby.
   *
   * @return A Set containing all user in this lobby
   */
  Set<User> getUsers();

  /**
   * Getter for all spectators in the lobby.
   *
   * @return A Set containing all spectators in this lobby
   */
  Set<User> getSpectators();

  /**
   * Getter Method for the number of players in a lobby without spectators.
   *
   * @return The number of players in the lobby
   */
  int getPlayerCount();

  /**
   * Getter for the ready status of a user.
   *
   * @param user The user to get the ready status from
   * @return returns a boolean for the ready status
   */
  boolean getReady(User user);

  /**
   * Setter for the ready status of a user.
   *
   * @param user user whose ready status is getting changed
   */
  void setReady(User user);

  /**
   * ready map of all user.
   *
   * @return all users are ready or not.
   */
  HashMap<User, Boolean> getReadyList();

  /**
   * Setter to update the LobbyOptions.
   *
   * @param options the new LobbyOptions
   * @return boolean if the update was successful
   */
  boolean updateOptions(LobbyOptions options);

  /**
   * Getter for the LobbyOptions.
   *
   * @return LobbyOptions
   */
  LobbyOptions getOptions();

  /**
   * Getter for the LobbyStatus.
   *
   * @return LobbyOptions.LobbyStatus
   */
  LobbyOptions.LobbyStatus getLobbyStatus();

  /** Setter for the LobbyStatus. */
  void setLobbyStatus(LobbyOptions.LobbyStatus lobbyStatus);

  /**
   * checks if all Users in the Lobby are ready.
   *
   * @return returns if all users are ready or not (true if all players are ready, false if they are
   *     not)
   */
  boolean checkReadyList();

  /** Setter for RobotSelected. */
  void setRobotSelected(Robots style, User user);

  /**
   * Getter for RobotSelected.
   *
   * @return boolean if Robot is already selected or not
   */
  boolean getRobotSelected(Robots style);

  /**
   * Getter for all RobotSelected.
   *
   * @return robotSelected array
   */
  List<Robots> getSelectedRobots();

  /**
   * Getter for the Style of a Robot of a User.
   *
   * @param user the User
   * @return the Style of the Robot
   * @see de.uol.swp.common.game.robot
   */
  Optional<Robots> getRobotStyle(User user);

  /**
   * Checks if a given user has selected a robot.
   *
   * @param user the user whose selection is to be checked
   * @return true if the user has selected a robot, false otherwise
   */
  default boolean isRobotSelected(User user) {
    return false;
  }

  /** Clears Robot Selections list so a new round can be played. */
  void clearRobotSelection();
}
