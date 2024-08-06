package de.uol.swp.client.lobby;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.common.game.floor.FloorPlanSetting;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.request.StartGameRequest;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.request.AllLobbyIdRequest;
import de.uol.swp.common.lobby.request.ChangeSpectatorModeRequest;
import de.uol.swp.common.lobby.request.CreateLobbyRequest;
import de.uol.swp.common.lobby.request.FloorPlansPreviewRequest;
import de.uol.swp.common.lobby.request.LobbyIdRequest;
import de.uol.swp.common.lobby.request.LobbyJoinAsSpectatorRequest;
import de.uol.swp.common.lobby.request.LobbyJoinUserRequest;
import de.uol.swp.common.lobby.request.LobbyKickUserRequest;
import de.uol.swp.common.lobby.request.LobbyLeaveUserRequest;
import de.uol.swp.common.lobby.request.LobbyOwnerRequest;
import de.uol.swp.common.lobby.request.ReadyCheckRequest;
import de.uol.swp.common.lobby.request.RetrieveLobbyOptionsRequest;
import de.uol.swp.common.lobby.request.RobotSelectionRequest;
import de.uol.swp.common.lobby.request.SelectedRobotsRequest;
import de.uol.swp.common.lobby.request.UpdateLobbyOptionsRequest;
import de.uol.swp.common.lobby.request.UserReadyRequest;
import de.uol.swp.common.user.UserDTO;
import java.awt.Point;
import java.util.EnumMap;
import java.util.Map;

/** Classes that manages lobbies. */
@SuppressWarnings("UnstableApiUsage")
public class LobbyService {

  private final EventBus eventBus;
  @Inject private LoggedInUser loggedInUser;

  /**
   * Constructor.
   *
   * @param eventBus The EventBus set in ClientModule
   * @see de.uol.swp.client.di.ClientModule
   */
  @Inject
  public LobbyService(EventBus eventBus) {
    this.eventBus = eventBus;
    this.eventBus.register(this);
  }

  /**
   * Posts a request to get a generated name for a new lobby.
   *
   * @see LobbyIdRequest
   */
  public void initiateNewLobby() {
    LobbyIdRequest lobbyIdRequest = new LobbyIdRequest();
    eventBus.post(lobbyIdRequest);
  }

  /**
   * Posts a request to create a lobby on the EventBus.
   *
   * @param user User who wants to create the new lobby
   * @see CreateLobbyRequest
   */
  public void createNewLobby(String name, UserDTO user, LobbyOptions lobbyOptions) {
    CreateLobbyRequest createLobbyRequest = new CreateLobbyRequest(name, user, lobbyOptions);
    eventBus.post(createLobbyRequest);
  }

  /**
   * Posts a request to join a specified lobby on the EventBus.
   *
   * @param lobbyId Name of the lobby the user wants to join
   * @param user User who wants to join the lobby
   * @see LobbyJoinUserRequest
   */
  public void joinLobby(String lobbyId, UserDTO user) {
    LobbyJoinUserRequest joinUserRequest = new LobbyJoinUserRequest(lobbyId, user);
    eventBus.post(joinUserRequest);
  }

  /**
   * Is called when user wants to join as spectator in LobbyListPresenter.
   *
   * @param lobbyId name of the lobby
   * @param user who wants to join as spectator
   */
  public void joinAsSpectator(String lobbyId, UserDTO user) {
    LobbyJoinAsSpectatorRequest lobbyJoinAsSpectatorRequest =
        new LobbyJoinAsSpectatorRequest(lobbyId, user);
    eventBus.post(lobbyJoinAsSpectatorRequest);
  }

  /** Retrieve the list of all current logged-in users. */
  public void retrieveAllLobbys() {
    AllLobbyIdRequest cmd = new AllLobbyIdRequest();
    eventBus.post(cmd);
  }

  /**
   * Posts a request to leave the current lobby on the EventBus.
   *
   * @param lobby Name of the lobby the user wants to leave
   * @param user User who wants to leave the lobby
   * @see LobbyLeaveUserRequest
   */
  public void leaveLobby(String lobby, UserDTO user) {
    LobbyLeaveUserRequest msg = new LobbyLeaveUserRequest(lobby, user);
    eventBus.post(msg);
  }

  /**
   * Sends signal that User is 'ready' inside lobby.
   *
   * @param lobbyId name of the Lobby
   * @param user user who clicked the ready button
   */
  public void sendReadySignal(String lobbyId, UserDTO user) {
    UserReadyRequest userReadyRequest = new UserReadyRequest(lobbyId, user);
    eventBus.post(userReadyRequest);
  }

  /**
   * Get lobby options of specific lobby.
   *
   * @param lobbyId the name of the lobby
   */
  public void retrieveLobbyOptions(String lobbyId) {
    eventBus.post(new RetrieveLobbyOptionsRequest(lobbyId));
  }

  /**
   * Post a request to kick a User from this lobby.
   *
   * @param lobby Name of the lobby the user get kicked from
   * @param username Username of User who get kicked
   */
  public void kickUser(String lobby, String username) {
    LobbyKickUserRequest request = new LobbyKickUserRequest(lobby, username);
    eventBus.post(request);
  }

  /**
   * Method to send LobbyOptions to the server. This method also validates all option beforehand. It
   * throws a UpdateLobbyOptionsExceptionMessage if the values are not in specific parameters.
   *
   * @param options LobbyOptions of the Lobby
   * @param lobbyId lobbyId of the Lobby
   * @param user the current user
   */
  @SuppressWarnings("java:S107")
  public void updateLobbyOptions(
      LobbyOptions options,
      String lobbyId,
      UserDTO user,
      int slots,
      String lobbyTitle,
      String turnLimit,
      boolean activeLasers,
      int aiPlayerCount,
      int[] aiDifficulty,
      boolean isWeakDuplicateActive,
      boolean isSwitchOffRoboterActive,
      FloorPlanSetting[] floorPlansSettings,
      Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition) {
    if (!options.setSlot(slots)) {
      eventBus.post(
          new UpdateLobbyOptionsExceptionMessage(
              "Slotbegrenzung muss zwischen 1 " + "und 8 liegen", null));
      return;
    }
    if (aiPlayerCount < 0 || aiPlayerCount > 8) {
      eventBus.post(
          new UpdateLobbyOptionsExceptionMessage(
              "Anzahl der AI Spieler muss zwischen 0 " + "und 6 liegen", null));
      return;
    }
    options.setLobbyTitle(lobbyTitle);
    options.setTurnLimit(turnLimit);
    options.setActiveLasers(activeLasers);
    options.setAiPlayerCount(aiPlayerCount);
    options.setWeakDuplicatedActive(isWeakDuplicateActive);
    options.setAiDifficulty(aiDifficulty);
    options.setSwitchOffRoboter(isSwitchOffRoboterActive);
    options.setFloorPlanSettings(floorPlansSettings);
    options.setCheckpointsPosition(checkpointsPosition);
    UpdateLobbyOptionsRequest msg = new UpdateLobbyOptionsRequest(lobbyId, user, options);
    eventBus.post(msg);
  }

  /**
   * Sends a request to update the FloorPlanPreview for all users in lobby.
   *
   * @param lobbyId the id of the lobby
   */
  public void sendFloorPlansPreviewRequest(String lobbyId) {
    eventBus.post(new FloorPlansPreviewRequest(lobbyId, loggedInUser.getUser()));
  }

  /**
   * Sends a ReadyCheckRequest to check if all users in the lobby are ready.
   *
   * @param lobbyId the lobby in which should be checked
   * @param user user who initiated the request
   */
  public void sendReadyCheck(String lobbyId, UserDTO user) {
    ReadyCheckRequest readyCheckRequest = new ReadyCheckRequest(lobbyId, user);
    eventBus.post(readyCheckRequest);
  }

  /**
   * Sends a signal to start the game.
   *
   * @param lobbyId Name of the lobby that wants to start the game.
   */
  public void sendStartSignal(String lobbyId) {
    eventBus.post(new StartGameRequest(lobbyId, loggedInUser.getUser()));
  }

  /**
   * Sends a request to get selected robots.
   *
   * @param lobbyId Name of the lobby
   */
  public void sendSelectedRobotsRequest(String lobbyId) {
    eventBus.post(new SelectedRobotsRequest(lobbyId, loggedInUser.getUser()));
  }

  /**
   * Sends request that LobbyOwner is requested.
   *
   * @param lobbyId is the lobby to the associated owner
   */
  public void sendLobbyOwnerRequest(String lobbyId, UserDTO user) {
    LobbyOwnerRequest lobbyOwnerRequest = new LobbyOwnerRequest(lobbyId, user);
    eventBus.post(lobbyOwnerRequest);
  }

  /**
   * Sends the Robot selection of the User.
   *
   * @param lobbyId in which Lobby a Robot have to be claimed or unclaimed
   * @param style which robot have to be claimed or unclaimed
   */
  public void sendRobotSelectionRequest(String lobbyId, Robots style, UserDTO user) {
    RobotSelectionRequest robotSelectionRequest = new RobotSelectionRequest(lobbyId, style, user);
    eventBus.post(robotSelectionRequest);
  }

  /**
   * Sends a request to change spectatorMode to true or false.
   *
   * @param lobbyId in which Lobby the user changes the mode
   * @param userDTO the user
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void sendChangeSpectatorModeRequest(
      String lobbyId, UserDTO userDTO, Robots robot, boolean robotSelected) {
    ChangeSpectatorModeRequest request =
        new ChangeSpectatorModeRequest(lobbyId, userDTO, robot, robotSelected);
    eventBus.post(request);
  }
}
