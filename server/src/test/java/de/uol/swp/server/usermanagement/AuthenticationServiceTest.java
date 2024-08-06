package de.uol.swp.server.usermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.request.LoginRequest;
import de.uol.swp.common.user.request.LogoutRequest;
import de.uol.swp.common.user.request.RetrieveAllOnlineUsersRequest;
import de.uol.swp.common.user.response.AllOnlineUsersResponse;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.server.lobby.LobbyManagement;
import de.uol.swp.server.message.ClientAuthorizedMessage;
import de.uol.swp.server.message.ClientLoggedOutMessage;
import de.uol.swp.server.message.ServerExceptionMessage;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import de.uol.swp.server.usermanagement.store.UserStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class AuthenticationServiceTest {

  final User user =
      new UserDTO(
          UUID.randomUUID().toString(), "name", "password", "email@test.de", new UserData(1));
  final User user2 =
      new UserDTO(
          UUID.randomUUID().toString(), "name2", "password2", "email@test.de2", new UserData(1));
  final User user3 =
      new UserDTO(
          UUID.randomUUID().toString(), "name3", "password3", "email@test.de3", new UserData(1));
  final UserStore userStore = new MainMemoryBasedUserStore();

  @SuppressWarnings("UnstableApiUsage")
  final EventBus bus = new EventBus();

  final UserManagement userManagement = new UserManagement(userStore);
  final AuthenticationService authService = new AuthenticationService(bus, userManagement);
  private final CountDownLatch lock = new CountDownLatch(1);
  LobbyManagement lobbyManagement = new LobbyManagement();
  private Object event;

  @Subscribe
  void onDeadEvent(DeadEvent e) {

    if (e.getEvent() instanceof ChatMessageMessage) {

    } else {
      this.event = e.getEvent();
      System.out.print(e.getEvent());
      lock.countDown();
    }
  }

  @BeforeEach
  void registerBus() {
    event = null;
    bus.register(this);
  }

  @AfterEach
  void deregisterBus() {
    bus.unregister(this);
  }

  @Test
  @SuppressWarnings({"java:S3415", "java:S5863"})
  void loginTest() throws InterruptedException {
    userManagement.createUser(user);
    final LoginRequest loginRequest = new LoginRequest(user.getUsername(), user.getPassword());
    bus.post(loginRequest);
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(userManagement.isLoggedIn(user));
    // is message send
    assertTrue(event instanceof ClientAuthorizedMessage);
    ClientAuthorizedMessage receivedMsg = (ClientAuthorizedMessage) event;
    assertEquals(user, receivedMsg.getUser());
    assertEquals(event, event);
    assertNotEquals(event, null);
    assertNotEquals(event, 10);
    ClientAuthorizedMessage equalUser = new ClientAuthorizedMessage(user);
    assertNotEquals(equalUser, event);
    equalUser.initWithMessage(receivedMsg);
    assertEquals(equalUser, event);
    assertEquals(equalUser.hashCode(), receivedMsg.hashCode());
    userManagement.dropUser(user);
  }

  @Test
  @SuppressWarnings({"java:S5845", "java:S3415", "java:S5863"})
  void loginTestFail() throws InterruptedException {
    userManagement.createUser(user);
    final LoginRequest loginRequest =
        new LoginRequest(user.getUsername(), user.getPassword() + "äüö");
    bus.post(loginRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertFalse(userManagement.isLoggedIn(user));
    assertTrue(event instanceof ServerExceptionMessage);
    ServerExceptionMessage serverExceptionMessage = (ServerExceptionMessage) event;
    Exception serverException = serverExceptionMessage.getException();
    assertEquals(
        "Einloggen von Nutzer nicht möglich.\n(Username oder Passwort falsch)",
        serverException.getMessage());

    assertEquals(serverExceptionMessage, serverExceptionMessage);
    assertNotEquals(serverExceptionMessage, null);
    assertNotEquals(serverExceptionMessage, 10);
    ServerExceptionMessage serverExceptionMessage2 = new ServerExceptionMessage(serverException);
    assertEquals(serverExceptionMessage2, serverExceptionMessage);
    assertEquals(serverExceptionMessage2.hashCode(), serverExceptionMessage.hashCode());

    userManagement.dropUser(user);
  }

  @Test
  void loginTestFailDouble() throws InterruptedException {
    userManagement.createUser(user);
    User newUser = userManagement.login(user.getUsername(), user.getPassword());
    assertTrue(userManagement.isLoggedIn(user));
    //
    final LoginRequest loginRequest = new LoginRequest(user.getUsername(), user.getPassword());
    bus.post(loginRequest);
    lock.await(1000, TimeUnit.MILLISECONDS);
    //
    assertTrue(event instanceof ServerExceptionMessage);
    assertEquals(
        "Der Spieler name ist bereits an einem anderen Client angemeldet.",
        ((ServerExceptionMessage) event).getException().getMessage());
    userManagement.dropUser(user);
  }

  @Test
  @SuppressWarnings({"java:S5845", "java:S5863", "java:S3415"})
  void logoutTest() throws InterruptedException {
    loginUser(user);
    Optional<Session> session = authService.getSession(user);

    assertTrue(session.isPresent());
    final LogoutRequest logoutRequest = new LogoutRequest();
    logoutRequest.setSession(session.get());

    bus.post(logoutRequest);

    lock.await(1000, TimeUnit.MILLISECONDS);

    assertFalse(userManagement.isLoggedIn(user));
    assertFalse(authService.getSession(user).isPresent());
    assertTrue(event instanceof ClientLoggedOutMessage);
    ClientLoggedOutMessage receivedMsg = (ClientLoggedOutMessage) event;
    assertEquals(user, receivedMsg.getUser());
    assertEquals(receivedMsg, receivedMsg);
    assertNotEquals(receivedMsg, null);
    assertNotEquals(receivedMsg, 10);
    ClientLoggedOutMessage equalUser = new ClientLoggedOutMessage(user);
    assertNotEquals(equalUser, receivedMsg);
    equalUser.initWithMessage(receivedMsg);
    assertEquals(equalUser, receivedMsg);
    assertEquals(equalUser.hashCode(), receivedMsg.hashCode());
  }

  private void loginUser(User userToLogin) {
    userManagement.createUser(userToLogin);
    final LoginRequest loginRequest =
        new LoginRequest(userToLogin.getUsername(), userToLogin.getPassword());
    bus.post(loginRequest);

    assertTrue(userManagement.isLoggedIn(userToLogin));
    userManagement.dropUser(userToLogin);
  }

  @Test
  void loggedInUsers() throws InterruptedException {
    loginUser(user);

    RetrieveAllOnlineUsersRequest request = new RetrieveAllOnlineUsersRequest();
    bus.post(request);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof AllOnlineUsersResponse);

    assertEquals(1, ((AllOnlineUsersResponse) event).getUsers().size());
    assertEquals(user, ((AllOnlineUsersResponse) event).getUsers().get(0));
  }

  @Test
  void twoLoggedInUsers() throws InterruptedException {
    List<User> users = new ArrayList<>();
    users.add(user);
    users.add(user2);
    Collections.sort(users);

    users.forEach(this::loginUser);

    RetrieveAllOnlineUsersRequest request = new RetrieveAllOnlineUsersRequest();
    bus.post(request);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof AllOnlineUsersResponse);

    List<User> returnedUsers = new ArrayList<>(((AllOnlineUsersResponse) event).getUsers());

    assertEquals(2, returnedUsers.size());

    Collections.sort(returnedUsers);
    assertEquals(returnedUsers, users);
  }

  @Test
  void loggedInUsersEmpty() throws InterruptedException {
    RetrieveAllOnlineUsersRequest request = new RetrieveAllOnlineUsersRequest();
    bus.post(request);

    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof AllOnlineUsersResponse);

    assertTrue(((AllOnlineUsersResponse) event).getUsers().isEmpty());
  }

  @Test
  void getSessionsForUsersTest() {
    loginUser(user);
    loginUser(user2);
    loginUser(user3);
    Set<User> users = new TreeSet<>();
    users.add(user);
    users.add(user2);
    users.add(user3);

    Optional<Session> session1 = authService.getSession(user);
    Optional<Session> session2 = authService.getSession(user2);
    Optional<Session> session3 = authService.getSession(user2);

    assertTrue(session1.isPresent());
    assertTrue(session2.isPresent());
    assertTrue(session3.isPresent());

    List<Session> sessions = authService.getSessions(users);

    assertEquals(3, sessions.size());
    assertTrue(sessions.contains(session1.get()));
    assertTrue(sessions.contains(session2.get()));
    assertTrue(sessions.contains(session3.get()));
  }
}
