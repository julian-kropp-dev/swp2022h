package de.uol.swp.client.chat;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.client.LoggedInUser;
import de.uol.swp.common.chat.request.ChatMessageRequest;
import de.uol.swp.common.user.UserDTO;
import de.uol.swp.common.user.userdata.UserData;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class ClientChatServiceTest {

  final UserDTO defaultUser =
      new UserDTO(UUID.randomUUID().toString(), "Marco", "test", "marco@test.de", new UserData(1));
  final String defaultMessage = "defaultMessage";
  final String defaultLobbyId = "lobbyName";
  final EventBus bus = new EventBus();
  final EventBus eventBus = new EventBus();
  final CountDownLatch lock = new CountDownLatch(1);
  final LoggedInUser loggedInUser = new LoggedInUser(eventBus);

  Object event;

  private ClientChatService clientChatService;

  /**
   * Handles DeadEvents detected on the EventBus If a DeadEvent is detected the event variable of
   * this class gets updated to its event and the event is printed to the console output.
   *
   * @param e The DeadEvent detected on the EventBus
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
    loggedInUser.setUser(defaultUser);
    clientChatService = new ClientChatService(bus, loggedInUser);
  }

  /**
   * Helper method run after each test case This method only unregisters the object of this class
   * from the EventBus.
   */
  @AfterEach
  void deregisterBus() {
    bus.unregister(this);
  }

  @Test
  void sendMessage() {
    clientChatService.sendMessage(defaultMessage, defaultUser, defaultLobbyId);
    assertTrue(event instanceof ChatMessageRequest);
  }

  @Test
  void sendMessageWithoutUser() {
    clientChatService.sendMessage(defaultMessage, defaultLobbyId);
    assertTrue(event instanceof ChatMessageRequest);
  }
}
