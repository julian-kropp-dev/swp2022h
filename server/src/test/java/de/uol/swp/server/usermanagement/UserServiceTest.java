package de.uol.swp.server.usermanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.common.chat.message.ChatMessageMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.request.RegisterUserRequest;
import de.uol.swp.common.user.request.RetrieveUserStatsRequest;
import de.uol.swp.common.user.response.RegistrationSuccessfulResponse;
import de.uol.swp.common.user.response.UpdateUserStatsResponse;
import de.uol.swp.common.user.userdata.UserData;
import de.uol.swp.common.user.userdata.UserStatistic;
import de.uol.swp.server.message.UpdateUserStatsMessage;
import de.uol.swp.server.usermanagement.store.MainMemoryBasedUserStore;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class UserServiceTest {

  static final User userToRegister =
      new UserDTO(
          UUID.randomUUID().toString(),
          "Marco",
          "Marco",
          "Marco@Grawunder.com",
          new UserData(1, Collections.singletonList(new UserStatistic(3, LocalDateTime.now()))));
  static final User userWithSameName =
      new UserDTO(
          UUID.randomUUID().toString(), "Marco", "Marco2", "Marco2@Grawunder.com", new UserData(1));

  final EventBus bus = new EventBus();
  final CountDownLatch lock = new CountDownLatch(1);
  final UserManagement userManagement = new UserManagement(new MainMemoryBasedUserStore());
  final UserService userService = new UserService(bus, userManagement);
  private Object event;

  @Subscribe
  void onDeadEvent(DeadEvent e) {
    if (!(e.getEvent() instanceof ChatMessageMessage)) {
      this.event = e.getEvent();
      lock.countDown();
    }
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

  @Test
  void registerUserTest() {
    final RegisterUserRequest request = new RegisterUserRequest(userToRegister);

    // The post will lead to a call of a UserService function
    bus.post(request);

    // can only test, if something in the state has changed
    final User loggedInUser =
        userManagement.login(userToRegister.getUsername(), userToRegister.getPassword());

    assertNotNull(loggedInUser);
    assertEquals(userToRegister, loggedInUser);
  }

  @Test
  void registerSecondUserWithSameName() {
    final RegisterUserRequest request = new RegisterUserRequest(userToRegister);
    final RegisterUserRequest request2 = new RegisterUserRequest(userWithSameName);

    bus.post(request);
    bus.post(request2);

    final User loggedInUser =
        userManagement.login(userToRegister.getUsername(), userToRegister.getPassword());

    // old user should be still in the store
    assertNotNull(loggedInUser);
    assertEquals(userToRegister, loggedInUser);

    // old user should not be overwritten!
    assertNotEquals(loggedInUser.getMail(), userWithSameName.getMail());
  }

  @Test
  @SuppressWarnings({"java:S5845", "java:S3415", "java:S5863"})
  void onUpdateUserStatistics() throws InterruptedException {
    final RegisterUserRequest request = new RegisterUserRequest(userToRegister);
    bus.post(request);
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof RegistrationSuccessfulResponse);
    UserStatistic userStatistic = new UserStatistic(1, LocalDateTime.now());
    UpdateUserStatsMessage userStatsMessage =
        new UpdateUserStatsMessage(userToRegister, userStatistic);
    assertEquals(1, userToRegister.getUserData().getUserStatistics().size());
    bus.post(userStatsMessage);
    assertEquals(3, userManagement.retrieveUserStatistics(userToRegister).get(0).getPlacement());
    //
    assertEquals(userToRegister, userStatsMessage.getUser());
    assertEquals(userStatistic, userStatsMessage.getUserStatistic());
    assertEquals(userStatsMessage, userStatsMessage);
    assertNotEquals(userStatsMessage, null);
    assertNotEquals(userStatsMessage, 10);
    UpdateUserStatsMessage userStatsMessage2 =
        new UpdateUserStatsMessage(userToRegister, userStatistic);
    assertEquals(userStatsMessage, userStatsMessage2);
    assertEquals(userStatsMessage.hashCode(), userStatsMessage2.hashCode());
  }

  @Test
  void onRetrieveUserStatsRequest() throws InterruptedException {
    final RegisterUserRequest request = new RegisterUserRequest(userToRegister);
    bus.post(request);
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof RegistrationSuccessfulResponse);
    RetrieveUserStatsRequest request2 = new RetrieveUserStatsRequest(userToRegister);
    bus.post(request2);
    lock.await(1000, TimeUnit.MILLISECONDS);
    assertTrue(event instanceof UpdateUserStatsResponse);
  }
}
