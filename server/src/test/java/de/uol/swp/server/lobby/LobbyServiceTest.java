package de.uol.swp.server.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.game.floor.FloorPlanSetting.FloorPlans;
import de.uol.swp.common.game.robot.Robots;
import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.lobby.exception.LobbyDoesNotAllowSpectatorsExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyDoesNotExistExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyIsFullExceptionMessage;
import de.uol.swp.common.lobby.exception.LobbyKickUserExceptionMessage;
import de.uol.swp.common.lobby.message.ChangedSpectatorModeMessage;
import de.uol.swp.common.lobby.message.UpdateLobbyListMessage;
import de.uol.swp.common.lobby.message.UserLeftLobbyMessage;
import de.uol.swp.common.lobby.message.UserReadyMessage;
import de.uol.swp.common.lobby.request.AllLobbyIdRequest;
import de.uol.swp.common.lobby.request.ChangeSpectatorModeRequest;
import de.uol.swp.common.lobby.request.CreateLobbyRequest;
import de.uol.swp.common.lobby.request.LobbyIdRequest;
import de.uol.swp.common.lobby.request.LobbyJoinAsSpectatorRequest;
import de.uol.swp.common.lobby.request.LobbyJoinUserRequest;
import de.uol.swp.common.lobby.request.LobbyKickUserRequest;
import de.uol.swp.common.lobby.request.LobbyLeaveUserRequest;
import de.uol.swp.common.lobby.request.LobbyOwnerRequest;
import de.uol.swp.common.lobby.request.ReadyCheckRequest;
import de.uol.swp.common.lobby.request.RetrieveLobbyOptionsRequest;
import de.uol.swp.common.lobby.request.UpdateLobbyOptionsRequest;
import de.uol.swp.common.lobby.request.UserReadyRequest;
import de.uol.swp.common.lobby.response.AllLobbyIdResponse;
import de.uol.swp.common.lobby.response.LobbyIdResponse;
import de.uol.swp.common.lobby.response.LobbyJoinAsSpectatorResponse;
import de.uol.swp.common.lobby.response.LobbyOwnerResponse;
import de.uol.swp.common.lobby.response.RetrieveLobbyOptionsResponse;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.message.ClientLoggedOutMessage;
import de.uol.swp.server.usermanagement.AuthenticationService;
import de.uol.swp.server.usermanagement.UserManagement;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored"})
class LobbyServiceTest {

  private static final List<UserDTO> users;

  static {
    users = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      users.add(
          new UserDTO(
              UUID.randomUUID().toString(),
              "julian" + i,
              "julian" + i,
              "julian" + i + "@test.de",
              new UserData(1)));
    }
    Collections.sort(users);
  }

  final CountDownLatch lock = new CountDownLatch(1);
  final User defaultUser =
      new UserDTO(UUID.randomUUID().toString(), "Marco", "test", "marco@test.de", new UserData(1));
  final User defaultUser2 =
      new UserDTO(UUID.randomUUID().toString(), "Marco2", "test", "marco@test.de", new UserData(1));
  final String lobbyTitle = "lobbyTitle";
  EventBus bus = new EventBus();
  UserManagement userManagement = getDefaultManagement();
  AuthenticationService authenticationService =
      new AuthenticationService(bus, new UserManagement(new MainMemoryBasedUserStore()));
  LobbyManagement lobbyManagement = new LobbyManagement();
  LobbyService lobbyService = new LobbyService(lobbyManagement, authenticationService, bus);
  private Object event;

  @Subscribe
  void onDeadEvent(DeadEvent e) {
    if (!(e.getEvent() instanceof ChatMessageMessage)) {
      this.event = e.getEvent();
      lock.countDown();
    }
  }

  UserManagement getDefaultManagement() {
    MainMemoryBasedUserStore store = new MainMemoryBasedUserStore();
    List<UserDTO> users = getDefaultUsers();
    users.forEach(
        u ->
            store.createUser(
                u.getUUID(), u.getUsername(), u.getPassword(), u.getMail(), u.getUserData()));
    return new UserManagement(store);
  }

  List<UserDTO> getDefaultUsers() {
    return Collections.unmodifiableList(users);
  }

  /**
   * Helper method run before each test case This method resets the variable event to null and
   * registers the object of this class to the EventBus.
   */
  @BeforeEach
  void registerBus() {
    event = null;
    bus.register(this);
  }

  /**
   * Helper method run after each test case This method only unregisters the object of this class
   * from the EventBus.
   *
   * @since 2019-10-10
   */
  @AfterEach
  void deregisterBus() {
    bus.unregister(this);
  }

  /**
   * Tests the onLobbyIdRequest method of the LobbyService class. Ensures that the event posted is
   * an instance of LobbyIdResponse.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onLobbyIdRequestTest() throws InterruptedException {
    LobbyIdRequest request = new LobbyIdRequest();
    bus.post(request);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyIdResponse);
  }

  /**
   * Tests the onLobbyOwnerRequest method of the LobbyService class. Ensures that the event posted
   * is an instance of LobbyOwnerResponse.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onLobbyOwnerRequestTest() throws InterruptedException {
    lobbyManagement.createLobby(lobbyTitle, defaultUser, new LobbyOptions());
    LobbyOwnerRequest lobbyOwnerRequest = new LobbyOwnerRequest(lobbyTitle, (UserDTO) defaultUser);
    bus.post(lobbyOwnerRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyOwnerResponse);
  }

  /**
   * Tests the onAllLobbyIdRequest method of the LobbyService class. Ensures that the event posted
   * is an instance of AllLobbyIdResponse.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onAllLobbyIdRequestTest() throws InterruptedException {
    lobbyManagement.createLobby(lobbyTitle, defaultUser, new LobbyOptions());
    lobbyManagement.createLobby("lobbyTitle2", defaultUser2, new LobbyOptions());
    lobbyManagement.createLobby("lobbyTitle3", defaultUser, new LobbyOptions());
    AllLobbyIdRequest allLobbyIdRequest = new AllLobbyIdRequest();
    bus.post(allLobbyIdRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof AllLobbyIdResponse);
  }

  /**
   * Tests the onUpdateLobbyOptionsRequest method of the LobbyService class. Ensures that the event
   * posted is an instance of UpdateLobbyListMessage and that the name and user properties are null.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onUpdateLobbyOptionsRequestTest() throws InterruptedException {
    // Setup scenario
    LobbyOptions options = new LobbyOptions();
    LobbyOptions options2 = new LobbyOptions();
    options2.setPrivateLobby(true);
    options2.setTurnLimit(2);
    options2.setCheckpointsPosition(
        new HashMap<>(
            Map.of(
                2,
                new EnumMap<>(Map.of(FloorPlans.CROSS, new Point(2, 3))),
                1,
                new EnumMap<>(Map.of(FloorPlans.CROSS, new Point(3, 3))))));
    options.setLobbyTitle("TestLobby");
    lobbyManagement.createLobby("TestLobby", defaultUser, options);

    UpdateLobbyOptionsRequest updateLobbyOptionsRequest =
        new UpdateLobbyOptionsRequest("TestLobby", (UserDTO) defaultUser, options2);
    bus.post(updateLobbyOptionsRequest);

    // Wait for the event to be processed
    lock.await(1000, TimeUnit.MILLISECONDS);

    // Assert that the expected event was posted
    assertTrue(event instanceof UpdateLobbyListMessage);
    UpdateLobbyListMessage updatedMessage = (UpdateLobbyListMessage) event;
    assertEquals("TestLobby", updatedMessage.getLobbyId());
    assertNull(updatedMessage.getUser());
  }

  /**
   * Tests the sendToAllInLobby method of the LobbyService class. Ensures that the event posted is
   * an instance of UserLeftLobbyMessage.
   */
  @Test
  void sendToAllInLobbyTest() {
    lobbyManagement.createLobby("TestLobby", defaultUser, new LobbyOptions());
    lobbyService.sendToAllInLobby("TestLobby", new UserLeftLobbyMessage());
    assertTrue(event instanceof UserLeftLobbyMessage);
  }

  /**
   * Tests the onUserReadyRequest method of the LobbyService class. Ensures that the event posted is
   * an instance of UserReadyMessage.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onUserReadyRequestTest() throws InterruptedException {
    lobbyManagement.createLobby("TestLobby", defaultUser, new LobbyOptions());
    UserReadyRequest userReadyRequest = new UserReadyRequest("TestLobby", (UserDTO) defaultUser);
    bus.post(userReadyRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof UserReadyMessage);
  }

  /**
   * Tests the onLobbyKickUserRequest method of the LobbyService class. Ensures that the event
   * posted is an instance of LobbyKickUserExceptionMessage. Also ensures that the owner of the
   * lobby is not changed if the kick request is not valid.
   */
  @Test
  void onLobbyKickUserRequestTest() {
    lobbyManagement.createLobby("TestLobby", defaultUser, new LobbyOptions());
    Optional<Lobby> lobbyOptional = lobbyManagement.getLobby("TestLobby");
    assertTrue(lobbyOptional.isPresent());
    assertEquals(1, lobbyOptional.get().getUsers().size());

    UserDTO testUser1 =
        new UserDTO(UUID.randomUUID().toString(), "testUser1", "test1", "testPw1", new UserData(1));
    bus.post(new LobbyJoinUserRequest("TestLobby", testUser1));
    lobbyOptional = lobbyManagement.getLobby("TestLobby");
    assertTrue(lobbyOptional.isPresent());
    assertEquals(2, lobbyOptional.get().getUsers().size());
    assertEquals(defaultUser.getUsername(), lobbyOptional.get().getOwner().getUsername());
    assertTrue(lobbyOptional.get().getUsers().contains(testUser1));

    LobbyKickUserRequest lobbyKickUserRequest =
        new LobbyKickUserRequest("TestLobby", testUser1.getUsername());
    bus.post(lobbyKickUserRequest);
    lobbyOptional = lobbyManagement.getLobby("TestLobby");
    assertTrue(lobbyOptional.isPresent());
    assertEquals(1, lobbyOptional.get().getUsers().size());

    LobbyKickUserRequest lobbyKickUserRequest1 = new LobbyKickUserRequest("TestLobby2", null);
    bus.post(lobbyKickUserRequest1);
    assertTrue(event instanceof LobbyKickUserExceptionMessage);

    LobbyKickUserRequest lobbyKickUserRequest2 =
        new LobbyKickUserRequest("TestLobby", defaultUser.getUsername());
    bus.post(lobbyKickUserRequest2);
    assertFalse(lobbyManagement.getLobby("LobbyTest").isPresent());
  }

  /**
   * Tests the onRetrieveLobbyOptionsRequest method of the LobbyService class. Ensures that the
   * event posted is an instance of RetrieveLobbyOptionsResponse.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onRetrieveLobbyOptionsRequestTest() throws InterruptedException {
    lobbyManagement.createLobby("TestLobby", defaultUser, new LobbyOptions());
    RetrieveLobbyOptionsRequest retrieveLobbyOptionsRequest =
        new RetrieveLobbyOptionsRequest("TestLobby");
    bus.post(retrieveLobbyOptionsRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof RetrieveLobbyOptionsResponse);
  }

  /**
   * Tests the onUserLoggedOutMessage method of the LobbyService class. Ensures that the event
   * posted is an instance of LobbyLeaveUserRequest.
   *
   * @throws InterruptedException if the lock wait is interrupted.
   */
  @Test
  void onUserLoggedOutMessage() throws InterruptedException {
    lobbyManagement.createLobby("Test", defaultUser2, new LobbyOptions());

    ClientLoggedOutMessage clientLoggedOutMessage = new ClientLoggedOutMessage(defaultUser2);
    bus.post(clientLoggedOutMessage);

    lock.await(1000, TimeUnit.MILLISECONDS);

    assertFalse(userManagement.isLoggedIn(defaultUser2));
  }

  /** Test onLobbyIdRequest method */
  @Test
  void createLobbyTest() throws InterruptedException {
    bus.post(new CreateLobbyRequest("test", (UserDTO) defaultUser, new LobbyOptions()));
    lock.await(1000, TimeUnit.MILLISECONDS);

    LobbyOptions options = new LobbyOptions();
    assertThrows(
        IllegalArgumentException.class,
        () -> lobbyManagement.createLobby("test", defaultUser, options));
    assertTrue(lobbyManagement.getLobby("test").isPresent());
    assertEquals(lobbyManagement.getLobby("test").get().getOwner(), defaultUser);
    assertTrue(lobbyManagement.getLobby("test").get().getUsers().contains(defaultUser));

    assertSame(
        LobbyOptions.LobbyStatus.WAITING, lobbyManagement.getLobby("test").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.READY, lobbyManagement.getLobby("test").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.STARTING, lobbyManagement.getLobby("test").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.INGAME, lobbyManagement.getLobby("test").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.END, lobbyManagement.getLobby("test").get().getLobbyStatus());
  }

  /**
   * Test Creation of multiple lobbys
   *
   * @throws InterruptedException thrown by lock.await()
   */
  @Test
  void createMultipleLobbysTest() throws InterruptedException {
    bus.post(new CreateLobbyRequest("test1", (UserDTO) defaultUser, new LobbyOptions()));
    bus.post(new CreateLobbyRequest("test2", (UserDTO) defaultUser, new LobbyOptions()));
    lock.await(1000, TimeUnit.MILLISECONDS);

    assertFalse(lobbyManagement.getLobby("test0").isPresent());
    assertTrue(lobbyManagement.getLobby("test1").isPresent());
    assertTrue(lobbyManagement.getLobby("test2").isPresent());
  }

  /** Test retrieveAllLobbys method */
  @Test
  void retrieveAllLobbysTest() {
    lobbyManagement.createLobby("retrieve", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("retrieve");
    assertTrue(lobby.isPresent());

    assertTrue(lobbyManagement.retrieveAllLobbies().contains(lobby.get()));
    assertFalse(
        lobbyManagement.retrieveAllLobbies().contains(new LobbyDTO("retrieve2", defaultUser)));
  }

  /** Test if onLobbyJoinUserRequest exception message (LobbyIsFull) works */
  @Test
  void onLobbyJoinUserRequestExceptionMessageTest() {
    lobbyManagement.createLobby("testLobby", defaultUser, new LobbyOptions());
    UserDTO testUser1 =
        new UserDTO(UUID.randomUUID().toString(), "testUser1", "test1", "testPw1", new UserData(1));
    UserDTO testUser2 =
        new UserDTO(UUID.randomUUID().toString(), "testUser2", "test2", "testPw2", new UserData(1));
    Optional<Lobby> lobby = lobbyManagement.getLobby("testLobby");
    assertTrue(lobby.isPresent());

    bus.post(new LobbyJoinUserRequest("testLobby", testUser1));
    assertEquals(2, lobby.get().getUsers().size());
    bus.post(new LobbyJoinUserRequest("testLobby", testUser2));
    assertEquals(2, lobby.get().getUsers().size());
  }

  /** Test onLobbyLeaveUserRequest method */
  @Test
  void onLobbyLeaveUserRequestTest() {
    lobbyManagement.createLobby("Lobby1", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("Lobby1");
    assertTrue(lobby.isPresent());
    lobby.get().joinUser(defaultUser2);

    assertSame(defaultUser, lobbyManagement.getLobby("Lobby1").get().getOwner());
    bus.post(new LobbyLeaveUserRequest("Lobby1", (UserDTO) defaultUser));
    assertNotSame(null, lobbyManagement.getLobby("Lobby1").get().getOwner());
  }

  /**
   * Test that a Lobby is dropped automatically when last User leaves via the LobbyLeaveUserRequest
   */
  @Test
  void onLobbyDroppedAutomaticallyTest() {
    lobbyManagement.createLobby("DeleteMe", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("DeleteMe");
    assertTrue(lobby.isPresent());
    lobby.get().joinUser(defaultUser2);

    lobby.get().leaveUser(defaultUser);
    assertEquals(1, lobby.get().getPlayerCount());

    bus.post(new LobbyLeaveUserRequest("DeleteMe", (UserDTO) defaultUser2));
    assertTrue(lobbyManagement.getLobby("DeleteMe").isEmpty());
  }

  /**
   * This test tests the onReadyCheckRequest method in LobbyService. Creates a new lobby, adding the
   * two default user. First, it sets defaultUser2 ready. Checks, that lobby is not ready yet,
   * because defaultUser1 is not ready yet Second, it sets defaultUser1 ready too and posts
   * ReadyCheckRequests on bus. Now Lobby status is no longer WAITING but READY
   */
  @Test
  void onReadyCheckRequestTest() {
    lobbyManagement.createLobby("ReadyCheck", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("ReadyCheck");
    assertTrue(lobby.isPresent());
    lobby.get().joinUser(defaultUser2);
    lobby.get().setReady(defaultUser2);

    assertFalse(lobby.get().checkReadyList());
    assertSame(
        LobbyOptions.LobbyStatus.WAITING,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());

    lobby.get().setReady(defaultUser);

    assertTrue(lobby.get().checkReadyList());

    ReadyCheckRequest rqst =
        new ReadyCheckRequest(lobby.get().getLobbyId(), (UserDTO) defaultUser2);
    bus.post(rqst);

    assertSame(
        LobbyOptions.LobbyStatus.READY,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.WAITING,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.INGAME,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.STARTING,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());
    assertNotSame(
        LobbyOptions.LobbyStatus.END,
        lobbyManagement.getLobby("ReadyCheck").get().getLobbyStatus());
  }

  /** test to check that a user cannot join lobby if lobby has not status WAITING */
  @Test
  void onLobbyJoinUserRequestLobbyNotWaiting() {
    lobbyManagement.createLobby("LobbyNotWaitingCheck", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("LobbyNotWaitingCheck");
    assertTrue(lobby.isPresent());
    lobby.get().setLobbyStatus(LobbyOptions.LobbyStatus.INGAME);
    assertSame(
        LobbyOptions.LobbyStatus.INGAME,
        lobbyManagement.getLobby("LobbyNotWaitingCheck").get().getLobbyStatus());

    assertEquals(1, lobby.get().getUsers().size());

    bus.post(new LobbyJoinUserRequest("LobbyNotWaitingCheck", (UserDTO) defaultUser2));

    assertEquals(1, lobby.get().getUsers().size());
  }

  /** test to check if a robot can be selected */
  @Test
  void onSelectRobotRequest() {
    lobbyManagement.createLobby("SelectCheck", defaultUser, new LobbyOptions());
    Optional<Lobby> lobby = lobbyManagement.getLobby("SelectCheck");
    assertTrue(lobby.isPresent());
    lobby.get().setRobotSelected(Robots.BOB, defaultUser);
    assertTrue(lobby.get().getRobotSelected(Robots.BOB));
  }

  /** test to check that spectators can join a lobby */
  @Test
  void onLobbyJoinAsSpectatorRequest_successful() throws InterruptedException {
    // Lobby does allow spectators
    LobbyOptions lobbyOptions = new LobbyOptions();
    lobbyManagement.createLobby("SpectatorRequest", defaultUser, lobbyOptions);
    Optional<Lobby> lobby = lobbyManagement.getLobby("SpectatorRequest");
    assertTrue(lobby.isPresent());
    bus.post(new LobbyJoinAsSpectatorRequest("SpectatorRequest", (UserDTO) defaultUser));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyJoinAsSpectatorResponse);
    // too many spectators
    lobbyOptions.setMaxSpectators(1);
    bus.post(new LobbyJoinAsSpectatorRequest("SpectatorRequest", (UserDTO) defaultUser2));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyIsFullExceptionMessage);
  }

  /** test to check that a lobby can lock out spectators */
  @Test
  void onLobbyJoinAsSpectatorRequest_unsuccessful() throws InterruptedException {
    // Lobby does not exist
    bus.post(new LobbyJoinAsSpectatorRequest("unknown", (UserDTO) defaultUser));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyDoesNotExistExceptionMessage);
    // Lobby does not allow spectators
    LobbyOptions lobbyOptions = new LobbyOptions();
    lobbyOptions.setSpectatorModeActive(false);
    lobbyManagement.createLobby("SpectatorRequest", defaultUser, lobbyOptions);
    Optional<Lobby> lobby = lobbyManagement.getLobby("SpectatorRequest");
    assertTrue(lobby.isPresent());
    bus.post(new LobbyJoinAsSpectatorRequest("SpectatorRequest", (UserDTO) defaultUser));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyDoesNotAllowSpectatorsExceptionMessage);
  }

  /** test to check that a player can change his spectator status */
  @Test
  void onChangeSpectatorModeRequest() throws InterruptedException {
    // Lobby does not exist
    bus.post(new ChangeSpectatorModeRequest("unknown", (UserDTO) defaultUser, Robots.BOB, true));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertEquals(null, event);
    // create Lobby
    LobbyOptions lobbyOptions = new LobbyOptions();
    lobbyManagement.createLobby("SpectatorRequest", defaultUser, lobbyOptions);
    Optional<Lobby> lobbyOptional = lobbyManagement.getLobby("SpectatorRequest");
    assertTrue(lobbyOptional.isPresent());
    Lobby lobby = lobbyOptional.get();
    lobby.setRobotSelected(Robots.BOB, defaultUser);
    lobby.joinUser(defaultUser2);
    lobby.setRobotSelected(Robots.GANDALF, defaultUser2);
    // Lobby does not allow spectators
    lobbyOptions.setSpectatorModeActive(false);
    bus.post(
        new ChangeSpectatorModeRequest(
            "SpectatorRequest", (UserDTO) defaultUser, Robots.BOB, true));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertEquals(null, event);
    //
    // Lobby does allow change to spectator mode
    lobbyOptions.setSpectatorModeActive(true);
    assertEquals(2, lobby.getPlayerCount());
    bus.post(
        new ChangeSpectatorModeRequest(
            "SpectatorRequest", (UserDTO) defaultUser, Robots.BOB, true));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof ChangedSpectatorModeMessage);
    assertEquals(1, lobby.getPlayerCount());
    //
    assertTrue(lobbyOptions.setSlot(1));
    bus.post(
        new ChangeSpectatorModeRequest(
            "SpectatorRequest", (UserDTO) defaultUser2, Robots.GANDALF, false));
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof LobbyIsFullExceptionMessage);
    assertEquals(1, lobby.getPlayerCount());
    //
    assertTrue(lobbyOptions.setSlot(2));
    bus.post(
        new ChangeSpectatorModeRequest(
            "SpectatorRequest", (UserDTO) defaultUser, Robots.BOB, true));
    lock.await(1000, TimeUnit.MILLISECONDS);
    //    assertTrue(event instanceof LobbyIsFullExceptionMessage);
    assertTrue(event instanceof ChangedSpectatorModeMessage);
    assertEquals(2, lobby.getPlayerCount());
  }
}
