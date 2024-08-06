package de.uol.swp.client.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.lobby.LobbyOptions;
import de.uol.swp.common.lobby.exception.UpdateLobbyOptionsExceptionMessage;
import de.uol.swp.common.lobby.request.AllLobbyIdRequest;
import de.uol.swp.common.lobby.request.CreateLobbyRequest;
import de.uol.swp.common.lobby.request.LobbyIdRequest;
import de.uol.swp.common.lobby.request.LobbyLeaveUserRequest;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for the LobbyService
 *
 * @see LobbyService
 */
@SuppressWarnings("UnstableApiUsage")
class LobbyServiceTest {

  final User defaultUser =
      new UserDTO(UUID.randomUUID().toString(), "Marco", "test", "marco@test.de", new UserData(1));
  final EventBus bus = new EventBus();
  final CountDownLatch lock = new CountDownLatch(1);
  Object event;

  /**
   * Handles DeadEvents detected on the EventBus If a DeadEvent is detected the event variable of
   * this class gets updated to its event and the event is printed to the console output.
   *
   * @param e The DeadEvent detected on the EventBus
   * @since 2019-10-10
   */
  @Subscribe
  void onDeadEvent(DeadEvent e) {
    this.event = e.getEvent();
    System.out.print(e.getEvent());
    lock.countDown();
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

  /** Test for the initiateNewLobby method */
  @Test
  void initiateNewLobby() {
    LobbyService lobbyService = new LobbyService(bus);
    lobbyService.initiateNewLobby();
    assertTrue(event instanceof LobbyIdRequest);
  }

  /** Test for the createNewLobby method */
  @Test
  void createNewLobby() {
    LobbyService lobbyService = new LobbyService(bus);
    lobbyService.createNewLobby("test", (UserDTO) defaultUser, new LobbyOptions());
    assertTrue(event instanceof CreateLobbyRequest);
    CreateLobbyRequest createLobbyRequest = (CreateLobbyRequest) event;
    assertEquals("test", createLobbyRequest.getLobbyId());
    assertEquals(defaultUser, createLobbyRequest.getUser());
  }

  /** Test retrieveAllLobbys method */
  @Test
  void retrieveAllLobbysTest() {
    LobbyService lobbyService = new LobbyService(bus);
    lobbyService.retrieveAllLobbys();
    assertTrue(event instanceof AllLobbyIdRequest);
  }

  /** Test for the leaveLobby method */
  @Test
  void leaveLobbyTest() {

    LobbyService lobbyService = new LobbyService(bus);
    lobbyService.createNewLobby("test", (UserDTO) defaultUser, new LobbyOptions());
    lobbyService.leaveLobby("test", (UserDTO) defaultUser);
    assertTrue(event instanceof LobbyLeaveUserRequest);
    LobbyLeaveUserRequest createLobbyRequest = (LobbyLeaveUserRequest) event;
    assertEquals("test", createLobbyRequest.getLobbyId());
    assertNotEquals("test2", createLobbyRequest.getLobbyId());
  }

  /**
   * This test checks if an exception is thrown when the ai slots surpass the maximum available
   * slots
   */
  @Test
  void setAiPlayerCountTest() {
    LobbyService lobbyService = new LobbyService(bus);
    lobbyService.createNewLobby("test", (UserDTO) defaultUser, new LobbyOptions());
    lobbyService.updateLobbyOptions(
        new LobbyOptions(),
        "test",
        (UserDTO) defaultUser,
        4,
        "test",
        "100",
        false,
        12,
        new int[] {1, 2, 3, 4, 5, 6, 7},
        false,
        false,
        null,
        null);
    assertTrue(event instanceof UpdateLobbyOptionsExceptionMessage);
  }
}
