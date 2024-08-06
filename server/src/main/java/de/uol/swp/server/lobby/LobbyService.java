package de.uol.swp.server.lobby;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.message.RoundFinishMessage;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.LobbyOptions.LobbyStatus;
import de.uol.swp.common.lobby.exception.LobbyDoesNotAllowSpectatorsExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyDoesNotExistExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyIsFullExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyKickUserExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyLeaveExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyNotWaitingAnymoreExceptionMessage;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.message.ChangedSpectatorModeMessage;
import de.uol.swp.common.lobby.message.FloorPlansPreviewMessage;
import de.uol.swp.common.lobby.message.KickedUserMessage;
import de.uol.swp.common.lobby.message.LobbyCreatedMessage;
import de.uol.swp.common.lobby.message.LobbyDroppedMessage;
import de.uol.swp.common.lobby.message.SelectedRobotsMessage;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.lobby.message.UpdatedLobbyOptionsMessage;
import de.uol.swp.common.lobby.message.UserJoinedLobbyMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.message.UserReadyMessage;
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
import de.uol.swp.common.lobby.response.AllLobbyIdResponse;
import de.uol.swp.common.lobby.response.LobbyIdResponse;
import de.uol.swp.common.lobby.response.LobbyJoinAsSpectatorResponse;
import de.uol.swp.common.lobby.response.LobbyJoinSuccessfulResponse;
import de.uol.swp.common.lobby.response.LobbyLeaveSuccessfulResponse;
import de.uol.swp.common.lobby.response.LobbyOwnerResponse;
import de.uol.swp.common.lobby.response.ReadyCheckResponse;
import de.uol.swp.common.lobby.response.RetrieveLobbyOptionsResponse;
import de.uol.swp.common.lobby.response.RobotSelectedResponse;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.server.AbstractService;
import de.uol.swp.server.lobby.generator.NameGenerator;
import de.uol.swp.server.message.ClientLoggedOutMessage;
import de.uol.swp.server.message.MoveRobotInternalMessage;
import de.uol.swp.server.message.PlayerLeftLobbyMessage;
import de.uol.swp.server.message.ReplaceUserWithAiMessage;
import de.uol.swp.server.usermanagement.AuthenticationService;
import java.awt.Point;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Handles the lobby requests send by the users. */
@SuppressWarnings("UnstableApiUsage")
@Singleton
public class LobbyService extends AbstractService {

  private static final Logger LOG = LogManager.getLogger(LobbyService.class);
  private static LobbyService instance = null;
  private final AuthenticationService authenticationService;
  private final LobbyManagement lobbyManagement;

  /**
   * Constructor.
   *
   * @param lobbyManagement the server-wide LobbyManagement
   * @param authenticationService the server-wide AuthenticationService
   * @param eventBus the server-wide EventBus
   */
  @Inject
  @SuppressWarnings("java:S3010")
  public LobbyService(
      LobbyManagement lobbyManagement,
      AuthenticationService authenticationService,
      EventBus eventBus) {
    super(eventBus);
    LOG.debug("LobbyService created: {}{}", this, (instance == null) ? "" : " - DUPLICATION!");
    this.lobbyManagement = lobbyManagement;
    this.authenticationService = authenticationService;
    instance = this;
  }

  /** Return to the old Instance of the LobbyService. */
  public static LobbyService getInstance() {
    return instance;
  }

  /**
   * When the ChatCommand finished a move command, the MoveRobotInternalMessage is received. This
   * method creates a RoundFinishMessage with the step and lobby information from the
   * MoveRobotInternalMessage and sends it to the clients in the lobby specified in the
   * MoveRobotInternalMessage.
   *
   * @param message The MoveRobotInternalMessage received by the event.
   */
  @Subscribe
  public void moveRobotInternalMessage(MoveRobotInternalMessage message) {
    ServerMessage message2 =
        new RoundFinishMessage(message.getStep(), message.getLobby().getLobbyId());
    sendToAllInLobby(message.getLobby().getLobbyId(), message2);
  }

  /**
   * Handels LobbyIdRequests found on the EventBus If a LobbyIdRequest is found, this method is
   * called. It sends a Response to all clients with a generated lobbyId
   *
   * @param lobbyIdRequest The LobbyIdRequest found on the EventBus
   * @see LobbyIdRequest
   * @see LobbyIdResponse
   */
  @Subscribe
  public void onLobbyIdRequest(LobbyIdRequest lobbyIdRequest) {
    ResponseMessage returnMessage = new LobbyIdResponse(NameGenerator.generate());
    lobbyIdRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
    post(returnMessage);
  }

  /**
   * Handles LobbyOwnerRequest found on the Eventbus If a LobbyOwnerRequest is found, this method is
   * called. It sends a response to a client with the lobbyOwner
   *
   * @param request LobbyOwnerRequest
   */
  @Subscribe
  public void onLobbyOwnerRequest(LobbyOwnerRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      ResponseMessage message =
          new LobbyOwnerResponse(request.getLobbyId(), (UserDTO) lobby.get().getOwner());
      message.initWithMessage(request);
      post(message);
    }
  }

  /**
   * Handles SelectedRobotsRequest found on the Eventbus If a SelectedRobotsRequest is found, this
   * method is called. It sends a response to a client with the selected Robots
   *
   * @param request SelectedRobotsRequest
   */
  @Subscribe
  public void onSelectedRobotsRequest(SelectedRobotsRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      SelectedRobotsMessage message =
          new SelectedRobotsMessage(
              request.getLobbyId(), request.getUser(), lobby.get().getSelectedRobots());
      message.initWithMessage(request);
      post(message);
    }
  }

  /**
   * Handles AllLobbyIdRequest found on the EventBus.
   *
   * <p>If a AllLobbyIdRequest is found, this method is called. It sends a Response to the Client,
   * who requested all the names for the lobbies.
   *
   * @param request The AllLobbyIdRequest
   * @see AllLobbyIdRequest
   */
  @Subscribe
  public void onAllLobbyIdRequest(AllLobbyIdRequest request) {
    Collection<Lobby> lobbies = lobbyManagement.retrieveAllLobbies();
    ResponseMessage responseMessage = new AllLobbyIdResponse(lobbies);
    responseMessage.initWithMessage(request);
    post(responseMessage);
  }

  /**
   * Handles CreateLobbyRequests found on the EventBus
   *
   * <p>If a CreateLobbyRequest is detected on the EventBus, this method is called. It creates a new
   * Lobby via the LobbyManagement using the parameters from the request and sends a
   * LobbyCreatedMessage to every connected user
   *
   * @param createLobbyRequest The CreateLobbyRequest found on the EventBus
   * @see de.uol.swp.server.lobby.LobbyManagement#createLobby(String, User,
   *     de.uol.swp.common.lobby.LobbyOptions)
   * @see de.uol.swp.common.lobby.message.LobbyCreatedMessage
   */
  @Subscribe
  public void onCreateLobbyRequest(CreateLobbyRequest createLobbyRequest) {
    createLobbyRequest.getLobbyOptions().setMaxSpectators(64);
    lobbyManagement.createLobby(
        createLobbyRequest.getLobbyId(),
        createLobbyRequest.getOwner(),
        createLobbyRequest.getLobbyOptions());
    sendToAll(
        new LobbyCreatedMessage(
            createLobbyRequest.getLobbyId(),
            (UserDTO) createLobbyRequest.getOwner(),
            createLobbyRequest.getLobbyOptions()));
  }

  /**
   * Handles LobbyJoinUserRequests found on the EventBus
   *
   * <p>If a LobbyJoinUserRequest is detected on the EventBus, this method is called. It adds a user
   * to a Lobby stored in the LobbyManagement and sends a UserJoinedLobbyMessage to every user in
   * the lobby. In the case that the lobby has no empty slots available it sends a
   * LobbyIsFullExceptionMessage to the user. User can only enter lobby if the lobby has status
   * WAITING
   *
   * @param lobbyJoinUserRequest The LobbyJoinUserRequest found on the EventBus
   * @see de.uol.swp.common.lobby.Lobby
   * @see de.uol.swp.common.lobby.message.UserJoinedLobbyMessage
   */
  @Subscribe
  public void onLobbyJoinUserRequest(LobbyJoinUserRequest lobbyJoinUserRequest) {
    String lobbyId = lobbyJoinUserRequest.getLobbyId();
    UserDTO joiningUser = lobbyJoinUserRequest.getUser();
    LOG.debug("Received LobbyJoinRequest for {} from {}", lobbyId, joiningUser.getUsername());

    Optional<Lobby> optionalLobby = lobbyManagement.getLobby(lobbyId);
    if (optionalLobby.isPresent()) {
      Lobby lobby = optionalLobby.get();
      if (lobby.getLobbyStatus() != LobbyOptions.LobbyStatus.WAITING) {
        ResponseMessage responseMessage =
            new LobbyNotWaitingAnymoreExceptionMessage("Die gewählte Lobby ist bereits gestartet");
        lobbyJoinUserRequest.getMessageContext().ifPresent(responseMessage::setMessageContext);
        post(responseMessage);
      } else {
        if (lobby.getPlayerCount() >= lobby.getOptions().getSlot()
            && !lobby.getUsers().contains(lobbyJoinUserRequest.getUser())) {
          ResponseMessage returnMessage =
              new LobbyIsFullExceptionMessage("Die gewählte Lobby ist voll");
          lobbyJoinUserRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
          post(returnMessage);
        } else {
          lobby.joinUser(joiningUser);
          ResponseMessage returnMessage =
              new LobbyJoinSuccessfulResponse(lobbyId, joiningUser, lobby.getOptions());
          lobbyJoinUserRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
          UpdateLobbyListMessage lobbyListMessage =
              new UpdateLobbyListMessage(
                  lobby.getLobbyId(),
                  lobby.getOptions().getLobbyTitle(),
                  lobby.getPlayerCount(),
                  lobby.getOptions().getSlot(),
                  lobby.getOptions().getLobbyStatus(),
                  lobby.getOwner().getUsername(),
                  lobby.getOptions().isActiveLasers(),
                  lobby.getOptions().isSwitchOffRoboter(),
                  lobby.getOptions().isWeakDuplicatedActive());
          post(returnMessage);
          sendToAllInLobby(lobbyId, new UserJoinedLobbyMessage(lobbyId, joiningUser));
          sendToAll(lobbyListMessage);
        }
      }
    } else {
      ResponseMessage returnMessage = new LobbyDoesNotExistExceptionMessage(lobbyId);
      lobbyJoinUserRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
      post(returnMessage);
    }
  }

  /**
   * Joins a user to lobby and flags the user as a spectator.
   *
   * @param request sent by joining spectator
   */
  @Subscribe
  public void onLobbyJoinAsSpectatorRequest(LobbyJoinAsSpectatorRequest request) {
    String lobbyId = request.getLobbyId();
    UserDTO joiningUser = request.getUser();
    LOG.debug(
        "Received LobbyJoinAsSpectatorRequest for {} from {}", lobbyId, joiningUser.getUsername());

    Optional<Lobby> lobby = lobbyManagement.getLobby(lobbyId);

    if (lobby.isPresent()) {
      if (lobby.get().getOptions().isSpectatorModeActive()
          && lobby.get().getSpectators().size() < lobby.get().getOptions().getMaxSpectators()) {
        lobby.get().joinSpectator(request.getUser());
        ResponseMessage responseMessage =
            new LobbyJoinAsSpectatorResponse(
                lobbyId, joiningUser, lobby.get().getOptions(), lobby.get().getLobbyStatus());
        request.getMessageContext().ifPresent(responseMessage::setMessageContext);
        post(responseMessage);
      } else if (lobby.get().getSpectators().size()
          >= lobby.get().getOptions().getMaxSpectators()) {
        ResponseMessage returnMessage = new LobbyIsFullExceptionMessage(lobbyId);
        request.getMessageContext().ifPresent(returnMessage::setMessageContext);
        post(returnMessage);
      } else {
        ResponseMessage returnMessage = new LobbyDoesNotAllowSpectatorsExceptionMessage(lobbyId);
        request.getMessageContext().ifPresent(returnMessage::setMessageContext);
        post(returnMessage);
      }
    } else {
      ResponseMessage returnMessage = new LobbyDoesNotExistExceptionMessage(lobbyId);
      request.getMessageContext().ifPresent(returnMessage::setMessageContext);
      post(returnMessage);
    }
  }

  /**
   * Handles LobbyLeaveUserRequests found on the EventBus
   *
   * <p>If a LobbyLeaveUserRequest is detected on the EventBus, this method is called. It removes a
   * user from a Lobby stored in the LobbyManagement and sends a UserLeftLobbyMessage to every user
   * in the lobby. If the last user was removed from the lobby, the lobby will be deleted via the
   * lobbyManagement and a LobbyDroppedMessage is posted on the EventBus
   *
   * @param lobbyLeaveUserRequest The LobbyJoinUserRequest found on the EventBus
   * @see Lobby
   * @see UserLeftLobbyMessage
   */
  @Subscribe
  public void onLobbyLeaveUserRequest(LobbyLeaveUserRequest lobbyLeaveUserRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Received new LobbyLeaveUserRequest message from user {}",
          lobbyLeaveUserRequest.getUser().getUsername());
    }
    ResponseMessage returnMessage;
    Optional<Lobby> optionalLobby = lobbyManagement.getLobby(lobbyLeaveUserRequest.getLobbyId());

    if (optionalLobby.isPresent()) {
      if (optionalLobby.get().getSpectators().contains(lobbyLeaveUserRequest.getUser())) {
        optionalLobby.get().leaveSpectator(lobbyLeaveUserRequest.getUser());
        returnMessage =
            (new LobbyLeaveSuccessfulResponse(
                optionalLobby.get().getLobbyId(), lobbyLeaveUserRequest.getUser()));
        lobbyLeaveUserRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
        post(returnMessage);
        return;
      }
      try {
        optionalLobby.get().leaveUser(lobbyLeaveUserRequest.getUser());
        if (optionalLobby.get().getLobbyStatus() == LobbyStatus.INGAME) {
          post(
              new ReplaceUserWithAiMessage(
                  lobbyLeaveUserRequest.getUser(), optionalLobby.get().getLobbyId()));
        }
        post(new PlayerLeftLobbyMessage(optionalLobby.get(), lobbyLeaveUserRequest.getUser()));
        sendToAllInLobby(
            lobbyLeaveUserRequest.getLobbyId(),
            new UserLeftLobbyMessage(
                lobbyLeaveUserRequest.getLobbyId(), lobbyLeaveUserRequest.getUser()));
        returnMessage =
            new LobbyLeaveSuccessfulResponse(
                optionalLobby.get().getLobbyId(), lobbyLeaveUserRequest.getUser());
        UpdateLobbyListMessage lobbyListMessage =
            new UpdateLobbyListMessage(
                optionalLobby.get().getLobbyId(),
                optionalLobby.get().getOptions().getLobbyTitle(),
                optionalLobby.get().getPlayerCount(),
                optionalLobby.get().getOptions().getSlot(),
                optionalLobby.get().getOptions().getLobbyStatus(),
                optionalLobby.get().getOwner().getUsername(),
                optionalLobby.get().getOptions().isActiveLasers(),
                optionalLobby.get().getOptions().isSwitchOffRoboter(),
                optionalLobby.get().getOptions().isWeakDuplicatedActive());
        sendToAll(lobbyListMessage);
      } catch (IllegalStateException e) {
        returnMessage =
            new LobbyLeaveExceptionMessage(
                lobbyLeaveUserRequest.getUser()
                    + " cannot leave "
                    + lobbyLeaveUserRequest.getLobbyId()
                    + " optionalLobby: user is not in optionalLobby");
      } finally {
        LOG.debug(
            "{} Users remaining in Lobby {} ",
            optionalLobby.get().getPlayerCount(),
            optionalLobby.get().getLobbyId());
        if (optionalLobby.get().getPlayerCount() == 0) {
          String lobbyIdToDrop = optionalLobby.get().getLobbyId();
          try {
            LOG.debug("Dropping Lobby {} because it is empty", lobbyIdToDrop);
            lobbyManagement.dropLobby(lobbyLeaveUserRequest.getLobbyId());
            sendToAll(new LobbyDroppedMessage(lobbyIdToDrop));
            NameGenerator.markAsUnused(lobbyIdToDrop);
          } catch (IllegalArgumentException e) {
            LOG.warn(
                "{} Users remaining in Lobby {}, but an error "
                    + "occurred while dropping this optionalLobby.",
                optionalLobby.get().getPlayerCount(),
                optionalLobby.get().getLobbyId());
          }
        }
      }
    } else {
      returnMessage =
          new LobbyLeaveExceptionMessage(
              lobbyLeaveUserRequest.getUser()
                  + " cannot leave "
                  + lobbyLeaveUserRequest.getLobbyId()
                  + " optionalLobby: optionalLobby does not exist");
    }
    lobbyLeaveUserRequest.getMessageContext().ifPresent(returnMessage::setMessageContext);
    post(returnMessage);
  }

  /**
   * Handles UpdateLobbyOptionsRequest found on the Eventbus. It updates the LobbyOption with the
   * new LobbyOption in the message.
   *
   * @param request UpdateLobbyOptionsRequest
   */
  @Subscribe
  public void onUpdateLobbyOptionsRequest(UpdateLobbyOptionsRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Got new UpdateLobbyOptionsRequest message");
    }
    ResponseMessage responseMessage = null;
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());

    if (lobby.isEmpty()) {
      responseMessage =
          new UpdateLobbyOptionsExceptionMessage(
              "Lobby " + request.getLobbyId() + " existiert nicht", request.getLobbyId());
    } else if (!lobby.get().getOwner().equals(request.getUser())) {
      responseMessage =
          new UpdateLobbyOptionsExceptionMessage(
              "Du bist nicht Besitzer der Lobby " + request.getLobbyId(), request.getLobbyId());
    } else if ((lobby.get().getOptions().getNumberFloorPlans() > 0)
        && (!isCheckpointSelectionIsValid(request.getOptions().getCheckpointsPosition()))) {
      responseMessage =
          new UpdateLobbyOptionsExceptionMessage(
              "Die CheckPoints sind nicht korrekt gesetzt", request.getLobbyId());
    } else if (!lobby.get().updateOptions(request.getOptions())) {
      responseMessage =
          new UpdateLobbyOptionsExceptionMessage(
              "Diese Einstellung ist nicht möglich", request.getLobbyId());
    }
    if (responseMessage != null) {
      request.getMessageContext().ifPresent(responseMessage::setMessageContext);
      post(responseMessage);
      return;
    }
    post(
        new UpdatedLobbyOptionsMessage(
            request.getLobbyId(), request.getUser(), request.getOptions()));
    UpdateLobbyListMessage lobbyListMessage =
        new UpdateLobbyListMessage(
            lobby.get().getLobbyId(),
            lobby.get().getOptions().getLobbyTitle(),
            lobby.get().getPlayerCount(),
            lobby.get().getOptions().getSlot(),
            lobby.get().getOptions().getLobbyStatus(),
            lobby.get().getOwner().getUsername(),
            lobby.get().getOptions().isActiveLasers(),
            lobby.get().getOptions().isSwitchOffRoboter(),
            lobby.get().getOptions().isWeakDuplicatedActive());
    sendToAll(lobbyListMessage);
  }

  private boolean isCheckpointSelectionIsValid(
      Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition) {
    if (!hasCheckpoints(checkpointsPosition)) {
      return false;
    } else {
      return areCheckpointsInOrder(checkpointsPosition);
    }
  }

  private boolean areCheckpointsInOrder(
      Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition) {
    if (!(checkpointsPosition.containsKey(1) && checkpointsPosition.containsKey(2))) {
      return false;
    }
    boolean inOrder = true;
    for (int i = 1; i <= 6; i++) {
      if (checkpointsPosition.containsKey(i)
          && (i != 1 && !checkpointsPosition.containsKey(i - 1))) {
        inOrder = false;
        break;
      }
    }
    return inOrder;
  }

  private boolean hasCheckpoints(Map<Integer, EnumMap<FloorPlans, Point>> checkpointsPosition) {
    for (int i = 1; i <= 6; i++) {
      if (checkpointsPosition.containsKey(i)) {
        return true;
      }
    }
    return false;
  }

  /**
   * sends signal that other users in the lobby will see the preview.
   *
   * @param request that was found on the eventBus
   */
  @Subscribe
  public void onFloorPlansPreviewRequest(FloorPlansPreviewRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      sendToAll(new FloorPlansPreviewMessage(request.getLobbyId(), request.getUser()));
    }
  }

  /**
   * Prepares a given ServerMessage to be sent to all players in the lobby and posts it on the
   * EventBus.
   *
   * @param lobbyId Id of the lobby the players are in
   * @param message the message to be sent to the users
   * @see de.uol.swp.common.message.ServerMessage
   */
  public void sendToAllInLobby(String lobbyId, ServerMessage message) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(lobbyId);

    if (lobby.isPresent()) {
      message.setReceiver(authenticationService.getSessions(lobby.get().getUsers()));
      post(message);
    }
  }

  /**
   * Handles the UserReadyRequest found on the eventbus. this method posts a userreadMessage to the
   * clients in the lobby.
   *
   * @param request The userReadyRequest found on the EventBus
   */
  @Subscribe
  public void onUserReadyRequest(UserReadyRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());

    if (lobby.isPresent() && !lobby.get().getSpectators().contains(request.getUser())) {
      lobby.get().setReady(request.getUser());
      sendToAllInLobby(
          request.getLobbyId(),
          new UserReadyMessage(
              request.getLobbyId(), request.getUser(), lobby.get().getReady(request.getUser())));
    }
  }

  /**
   * Handles the LobbyKickUserRequest found on the EventBus.
   *
   * <p>It removes the kicked User from the lobby and send a UserLeftLobbyMessage to all remaining
   * players in the lobby. It also sends a KickedUserMessage to all Clients
   *
   * @param request The KickedUserMessage found on the EventBus
   */
  @Subscribe
  public void onLobbyKickUserRequest(LobbyKickUserRequest request) {
    LOG.debug("Received LobbyKickUserRequest, Kicking User: {}", request.getUsername());
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    ResponseMessage responseMessage = null;
    if (lobby.isPresent()) {
      User userToKick = null;
      String usernameFromRequest = request.getUsername();
      Set<User> usersInLobby = lobby.get().getUsers();
      for (User user : usersInLobby) {
        if (user.getUsername().equals(usernameFromRequest)) {
          userToKick = user;
          LOG.debug("Found user object for username {}", usernameFromRequest);
        }
      }
      if (userToKick != null && !userToKick.equals(lobby.get().getOwner())) {
        try {
          lobby.get().leaveUser(userToKick);
          sendToAllInLobby(
              request.getLobbyId(),
              new UserLeftLobbyMessage(request.getLobbyId(), (UserDTO) userToKick));
          sendToAll(new KickedUserMessage(request.getLobbyId(), (UserDTO) userToKick));
          sendToAll(
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
        } catch (IllegalStateException e) {
          responseMessage =
              new LobbyKickUserExceptionMessage(
                  "Cannot kick " + usernameFromRequest + " form lobby " + request.getLobbyId());
          post(responseMessage);
        }
      }
    } else {
      responseMessage =
          new LobbyKickUserExceptionMessage(
              "Cannot kick " + request.getUsername() + " form lobby " + request.getLobbyId());
    }
    post(responseMessage);
  }

  /**
   * Handles a RetrieveLobbyOptionsRequest and sends back the lobby options of a specific lobby to
   * that specific client.
   *
   * @param request the RetrieveLobbyOptionsRequest found on the eventBus
   */
  @Subscribe
  public void onRetrieveLobbyOptionsRequest(RetrieveLobbyOptionsRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      ResponseMessage responseMessage =
          new RetrieveLobbyOptionsResponse(
              lobby.get().getOptions(),
              lobby.get().getLobbyId(),
              lobby.get().getUsers(),
              lobby.get().getReadyList(),
              lobby.get().getSpectators());
      request.getMessageContext().ifPresent(responseMessage::setMessageContext);
      post(responseMessage);
    }
  }

  /**
   * Catches the ReadyCheckRequest and sends a ReadyCheckResponse with the result of the ready check
   * back to the owner of the lobby. If all users are ready to play the lobby status is set to
   * ready. Else lobby status is set back to waiting
   *
   * @param request the ReadyCheckRequest found on the Eventbus.
   */
  @Subscribe
  public void onReadyCheckRequest(ReadyCheckRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      ResponseMessage responseMessage =
          new ReadyCheckResponse(
              request.getLobbyId(), (UserDTO) lobby.get().getOwner(), lobby.get().checkReadyList());
      responseMessage.initWithMessage(request);
      LobbyOptions options = lobby.get().getOptions();
      if (lobby.get().checkReadyList()) {
        options.setLobbyStatus(LobbyOptions.LobbyStatus.READY);
      } else {
        options.setLobbyStatus(LobbyOptions.LobbyStatus.WAITING);
      }
      UpdateLobbyListMessage lobbyListMessage =
          new UpdateLobbyListMessage(
              lobby.get().getLobbyId(),
              lobby.get().getOptions().getLobbyTitle(),
              lobby.get().getPlayerCount(),
              lobby.get().getOptions().getSlot(),
              lobby.get().getOptions().getLobbyStatus(),
              lobby.get().getOwner().getUsername(),
              lobby.get().getOptions().isActiveLasers(),
              lobby.get().getOptions().isSwitchOffRoboter(),
              lobby.get().getOptions().isWeakDuplicatedActive());
      sendToAll(lobbyListMessage);
      lobby.get().updateOptions(options);
      post(responseMessage);
    }
  }

  /**
   * This method removes the user from all lobbies when they log out.
   *
   * @param msg The message that tells everyone in the server that a user has logged out.
   */
  @Subscribe
  public void onUserLoggedOutMessage(ClientLoggedOutMessage msg) {
    User user = msg.getUser();
    lobbyManagement.retrieveAllLobbies().parallelStream()
        .filter(t -> t.getUsers().contains(user))
        .filter(t -> !t.getSpectators().contains(user))
        .forEach(
            t -> {
              LobbyLeaveUserRequest lobbyLeaveUserRequest =
                  new LobbyLeaveUserRequest(t.getLobbyId(), (UserDTO) user);
              onLobbyLeaveUserRequest(lobbyLeaveUserRequest);
            });
  }

  /**
   * This method will select or unselect Robot in a specific Lobby.
   *
   * @param request the SelectRobotrequest found on the Eventbus
   */
  @Subscribe
  public void onSelectRobotRequest(RobotSelectionRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent()) {
      if (lobby.get().getSpectators().contains(request.getUser())) {
        LOG.debug("User who sent request is not a player in the Lobby.");
        return;
      }
      if (lobby.get().getRobotSelected(request.getStyle())) {
        if (lobby.get().isRobotSelected(request.getUser())) {
          lobby.get().setRobotSelected(request.getStyle(), request.getUser());
          ResponseMessage responseMessage =
              new RobotSelectedResponse(
                  request.getLobbyId(), false, request.getStyle(), false, request.getUser());
          responseMessage.initWithMessage(request);
          post(responseMessage);
        } else {
          ResponseMessage responseMessage =
              new RobotSelectedResponse(
                  request.getLobbyId(), true, request.getStyle(), true, request.getUser());
          responseMessage.initWithMessage(request);
          post(responseMessage);
        }

      } else {
        lobby.get().setRobotSelected(request.getStyle(), request.getUser());
        ResponseMessage responseMessage =
            new RobotSelectedResponse(
                request.getLobbyId(), false, request.getStyle(), true, request.getUser());
        responseMessage.initWithMessage(request);
        post(responseMessage);
      }
      sendToAllInLobby(
          request.getLobbyId(),
          new SelectedRobotsMessage(
              request.getLobbyId(), request.getUser(), lobby.get().getSelectedRobots()));
    }
  }

  /**
   * Changes to spectatorMode oder playerMode for user.
   *
   * @param request received from user in lobby
   */
  @Subscribe
  public void onChangeSpectatorModeRequest(ChangeSpectatorModeRequest request) {
    Optional<Lobby> lobby = lobbyManagement.getLobby(request.getLobbyId());
    if (lobby.isPresent() && lobby.get().getOptions().isSpectatorModeActive()) {
      ServerMessage serverMessage;
      boolean isSpectator;
      if (!lobby.get().getSpectators().contains(request.getUser())
          && lobby.get().getPlayerCount() > 1) {
        lobby.get().leaveUser(request.getUser());
        lobby.get().joinSpectator(request.getUser());
        LOG.debug("User {} left as player and is now spectator.", request.getUser().getUsername());
        if (lobby.get().isRobotSelected(request.getUser())) {
          lobby.get().setRobotSelected(request.getRobot(), request.getUser());
        }
        isSpectator = true;
      } else if (lobby.get().getPlayerCount() < lobby.get().getOptions().getSlot()) {
        lobby.get().leaveSpectator(request.getUser());
        lobby.get().joinUser(request.getUser());
        LOG.debug("User {} joined from spectator to real player.", request.getUser().getUsername());
        isSpectator = false;
      } else {
        ResponseMessage returnMessage =
            new LobbyIsFullExceptionMessage(
                "Du kannst diese Aktion nicht durchführen, "
                    + "da die maximale Spieleranzahl bereits erreicht wurde.");
        request.getMessageContext().ifPresent(returnMessage::setMessageContext);
        post(returnMessage);
        LOG.debug("{} can't leave or join as player.", request.getUser().getUsername());
        return;
      }
      sendToAll(
          new UpdateLobbyListMessage(
              lobby.get().getLobbyId(),
              lobby.get().getOptions().getLobbyTitle(),
              lobby.get().getUsers().size(),
              lobby.get().getOptions().getSlot(),
              lobby.get().getOptions().getLobbyStatus(),
              lobby.get().getOwner().getUsername(),
              lobby.get().getOptions().isActiveLasers(),
              lobby.get().getOptions().isSwitchOffRoboter(),
              lobby.get().getOptions().isWeakDuplicatedActive()));
      serverMessage =
          new ChangedSpectatorModeMessage(
              lobby.get().getLobbyId(), request.getUser(), isSpectator, lobby.get().getOwner());
      sendToAllInLobby(lobby.get().getLobbyId(), serverMessage);
    }
  }
}
