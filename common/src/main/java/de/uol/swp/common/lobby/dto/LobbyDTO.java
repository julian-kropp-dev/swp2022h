package de.uol.swp.common.lobby.dto;

import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.user.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Object to transfer the information of a game lobby.
 *
 * <p>This object is used to communicate the current state of game lobbies between the server and
 * clients. It contains information about the Name of the lobby, who owns the lobby and who joined
 * the lobby.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class LobbyDTO implements Lobby, Serializable {

  private static final long serialVersionUID = -8497600184486005098L;
  private final String lobbyId;
  private final Set<User> users = new TreeSet<>();
  private final Set<User> spectators = new TreeSet<>();
  private final LobbyOptions options;
  private final HashMap<User, Boolean> ready = new HashMap<>();
  private final List<Robots> robotSelected = new ArrayList<>();
  private final ConcurrentHashMap<User, Robots> playerRobot = new ConcurrentHashMap<>();
  private User owner;

  /**
   * Constructor with new LobbyOptions.
   *
   * @param lobbyId The name the lobby should have
   * @param creator The user who created the lobby and therefore shall be the owner
   */
  public LobbyDTO(String lobbyId, User creator) {
    this(lobbyId, creator, new LobbyOptions());
  }

  /**
   * Constructor.
   *
   * @param lobbyId the name of the lobby
   * @param creator the creator of the lobby
   * @param options the LobbyOptions
   */
  public LobbyDTO(String lobbyId, User creator, LobbyOptions options) {
    this.lobbyId = lobbyId;
    this.owner = creator;
    this.users.add(creator);
    this.ready.put(creator, false);
    this.options = options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LobbyDTO lobbyDTO = (LobbyDTO) o;
    return lobbyId.equals(lobbyDTO.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }

  @Override
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Sets the readyStatus for user on false on joining.
   *
   * @param user The new user to add to the lobby
   */
  @Override
  public void joinUser(User user) {
    this.users.add(user);
    this.ready.put(user, false);
  }

  /**
   * Joins a user to the Lobby who is flagged as spectator.
   *
   * @param user The new spectator to add to the lobby
   */
  @Override
  public void joinSpectator(User user) {
    this.spectators.add(user);
    this.users.add(user);
  }

  /**
   * Removes a User from the Lobby.
   *
   * @param user The user to remove from the lobby
   */
  @Override
  public void leaveUser(User user) {
    if (!users.contains(user)) {
      throw new IllegalStateException("User is not in lobby");
    }
    Optional<Robots> selectedRobot = getRobotStyle(user);
    if (selectedRobot.isPresent()) {
      robotSelected.remove(selectedRobot.get());
      playerRobot.remove(user);
    }
    this.users.remove(user);
    this.ready.remove(user);
    setRobotSelected(playerRobot.get(user), user);
    if (!users.isEmpty() && this.owner.equals(user)) {
      updateOwner(users.iterator().next());
    }
  }

  /**
   * Removes a spectator from the lobby.
   *
   * @param user The spectator to remove from the lobby
   */
  @Override
  public void leaveSpectator(User user) {
    if (!spectators.contains(user)) {
      throw new IllegalStateException("Spectator is not in lobby");
    }
    spectators.remove(user);
    if (!users.contains(user)) {
      throw new IllegalStateException("User is not in lobby");
    }
    this.users.remove(user);
  }

  @Override
  public void updateOwner(User user) {
    if (!this.users.contains(user)) {
      throw new IllegalArgumentException(
          "User " + user.getUsername() + "not found. Owner must be member of lobby!");
    }
    if (!this.spectators.contains(user)) {
      this.owner = user;
    }
  }

  @Override
  public User getOwner() {
    return owner;
  }

  @Override
  public Set<User> getUsers() {
    return Collections.unmodifiableSet(users);
  }

  @Override
  public Set<User> getSpectators() {
    return spectators;
  }

  @Override
  public int getPlayerCount() {
    return Collections.unmodifiableSet(users).size()
        - Collections.unmodifiableSet(spectators).size();
  }

  /**
   * Gives the ready status from user.
   *
   * @param user returns if a user is 'ready' or not
   */
  @Override
  public boolean getReady(User user) {
    return ready.get(user);
  }

  /**
   * When the value 'ready' is set to 'false' (which is the default value), it will change to
   * 'true'. And the other way around as well, if the User is already on 'ready'
   *
   * @param user The user
   */
  @Override
  public void setReady(User user) {
    boolean userReady = ready.get(user);
    this.ready.replace(user, userReady, !userReady);
  }

  /**
   * checks the ready map if all users are ready.
   *
   * @return returns if all users are ready or not.
   */
  @Override
  public boolean checkReadyList() {
    if (ready.containsValue(false)) {
      return false;
    }
    return !users.isEmpty();
  }

  /**
   * ready map of all user.
   *
   * @return all users are ready or not.
   */
  @Override
  public HashMap<User, Boolean> getReadyList() {
    return ready;
  }

  /**
   * Updates the LobbyOptions of a lobby.
   *
   * @param options the new LobbyOptions
   * @return if the update was successful
   */
  @Override
  public boolean updateOptions(LobbyOptions options) {
    if (options.getSlot() < this.getPlayerCount()) {
      return false;
    }
    if (options.getSlot() + options.getAiPlayerCount() > 8) {
      return false;
    }
    this.options.setLobbyTitle(options.getLobbyTitle());
    this.options.setTurnLimit(options.getTurnLimit());
    this.options.setActiveLasers(options.isActiveLasers());
    this.options.setAiPlayerCount(options.getAiPlayerCount());
    this.options.setWeakDuplicatedActive(options.isWeakDuplicatedActive());
    this.options.setAiDifficulty(options.getAiDifficulty());
    this.options.setSwitchOffRoboter(options.isSwitchOffRoboter());
    this.options.setFloorPlanSettings(options.getFloorPlanSettings());
    this.options.setCheckpointsPosition(options.getCheckpointsPosition());
    return this.options.setSlot(options.getSlot());
    // Check other options
  }

  /**
   * Getter for the LobbyOptions.
   *
   * @return the LobbyOptions
   */
  @Override
  public LobbyOptions getOptions() {
    return this.options;
  }

  /**
   * Getter for the LobbyStatus.
   *
   * @return the LobbyStatus
   */
  @Override
  public LobbyOptions.LobbyStatus getLobbyStatus() {
    return options.getLobbyStatus();
  }

  /** Setter for the LobbyStatus. */
  @Override
  public void setLobbyStatus(LobbyOptions.LobbyStatus lobbyStatus) {
    options.setLobbyStatus(lobbyStatus);
  }

  /**
   * Getter for Robotselected.
   *
   * @param style which selected status should be returned
   * @return boolean if Robot is already selected or not
   */
  @Override
  public boolean getRobotSelected(Robots style) {
    return robotSelected.contains(style);
  }

  /**
   * Getter for all Robotselected.
   *
   * @return robotSelected array
   */
  @Override
  public List<Robots> getSelectedRobots() {
    return robotSelected;
  }

  @Override
  public Optional<Robots> getRobotStyle(User user) {
    return Optional.ofNullable(playerRobot.get(user));
  }

  /**
   * Setter for RobotSelected.
   *
   * @param style Which robot will be selected or unselected
   */
  @Override
  public void setRobotSelected(Robots style, User user) {
    if (playerRobot.get(user) == style) {
      playerRobot.remove(user);
      robotSelected.remove(style);
    } else {
      playerRobot.put(user, style);
      robotSelected.add(style);
    }
  }

  @Override
  public boolean isRobotSelected(User user) {
    return playerRobot.containsKey(user);
  }

  @Override
  public void clearRobotSelection() {
    playerRobot.clear();
    robotSelected.clear();
  }
}
