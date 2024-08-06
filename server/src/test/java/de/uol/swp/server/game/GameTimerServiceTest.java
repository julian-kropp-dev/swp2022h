package de.uol.swp.server.game;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.uol.swp.server.message.GameTimerMessage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("UnstableApiUsage")
class GameTimerServiceTest {
  private final CountDownLatch lock = new CountDownLatch(1);
  private GameTimerService gameTimerService;
  private Object event;
  private EventBus bus;
  private String testLobbyId = "ABCD";

  @Subscribe
  public void onGameTimerMessage(GameTimerMessage msg) {
    event = msg;
  }

  @BeforeEach
  void setUp() {
    bus = new EventBus();
    gameTimerService = new GameTimerService(bus);
    bus.register(this);
  }

  @AfterEach
  void afterEach() {
    bus.unregister(this);
  }

  @Test
  @SuppressWarnings({"java:S5863", "java:S3415"})
  void gameTimer() throws InterruptedException {
    gameTimerService.gameTimer(1000, testLobbyId);
    lock.await(1200, TimeUnit.MILLISECONDS);
    lock.countDown();
    assertTrue(event instanceof GameTimerMessage);
    GameTimerMessage receivedMsg = (GameTimerMessage) event;
    assertEquals(testLobbyId, receivedMsg.getLobbyId());
    assertEquals("GameTimerMessage{gameName='" + testLobbyId + "'}", receivedMsg.toString());
    assertEquals(event, event);
    assertNotEquals(event, null);
    assertNotEquals(event, 10);
    GameTimerMessage equalLobby = new GameTimerMessage(testLobbyId);
    assertEquals(equalLobby, event);
    assertEquals(equalLobby.hashCode(), receivedMsg.hashCode());
  }
}
