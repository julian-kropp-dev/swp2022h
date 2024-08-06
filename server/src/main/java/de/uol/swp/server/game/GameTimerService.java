package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.server.message.GameTimerMessage;
import de.uol.swp.server.message.RespawnInteractionTimeMessage;
import de.uol.swp.server.message.WaitOnGuiTimerMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service for creating game timers that trigger an event after a specified amount of time has
 * elapsed.
 */
@SuppressWarnings("UnstableApiUsage")
public class GameTimerService {
  private static final Logger LOG = LogManager.getLogger(GameTimerService.class);
  private final EventBus bus;
  private final Map<String, List<Thread>> threads = new HashMap<>();

  /**
   * Creates a new GameTimerService with the specified EventBus.
   *
   * @param bus the event bus used for communication between components
   */
  public GameTimerService(EventBus bus) {
    this.bus = bus;
  }

  /**
   * Creates a new game timer with the specified time and triggers an event when the timer has
   * elapsed. The timer is executed in a separate thread.
   *
   * @param timeInMilliseconds the amount of time until the timer elapses, in milliseconds
   * @param gameId the name of the game for which the timer is being created
   */
  public void gameTimer(long timeInMilliseconds, String gameId) {
    Thread t =
        new Thread(
            () -> {
              try {
                Thread.sleep(timeInMilliseconds);
                bus.post(new GameTimerMessage(gameId));
                threads.get(gameId).remove(Thread.currentThread());
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    if (threads.containsKey(gameId)) {
      threads.get(gameId).add(t);
    } else {
      threads.put(gameId, new ArrayList<>(List.of(t)));
    }
    LOG.debug("Starting gameTimer: {}s", timeInMilliseconds / 1000);
    t.start();
  }

  /**
   * Creates a new timer with the specified time and triggers an event when the timer has elapsed.
   * The timer is executed in a separate thread.
   *
   * @param timeInMilliseconds the amount of time until the timer elapses, in milliseconds
   * @param gameId the Id of the game for which the timer is being created
   */
  public void respawnInteractionTimer(long timeInMilliseconds, String gameId) {
    Thread t =
        new Thread(
            () -> {
              try {
                Thread.sleep(timeInMilliseconds);
                bus.post(new RespawnInteractionTimeMessage(gameId));
                threads.get(gameId).remove(Thread.currentThread());
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    if (threads.containsKey(gameId)) {
      threads.get(gameId).add(t);
    } else {
      threads.put(gameId, new ArrayList<>(List.of(t)));
    }
    LOG.debug("Starting respawnInteractionTimer: {}s", timeInMilliseconds / 1000);
    t.start();
  }

  /**
   * Creates a new timer with the specified time and triggers an event when the timer has elapsed.
   * The timer is executed in a separate thread.
   *
   * @param timeInMilliseconds the amount of time until the timer elapses, in milliseconds
   * @param gameId the id of the game for which the timer is being created
   */
  public void waitOnGuiTimer(long timeInMilliseconds, String gameId) {
    Thread t =
        new Thread(
            () -> {
              try {
                Thread.sleep(timeInMilliseconds);
                bus.post(new WaitOnGuiTimerMessage(gameId));
                threads.get(gameId).remove(Thread.currentThread());
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    if (threads.containsKey(gameId)) {
      threads.get(gameId).add(t);
    } else {
      threads.put(gameId, new ArrayList<>(List.of(t)));
    }
    LOG.debug("Starting waitOnGuiTimer: {}s", timeInMilliseconds / 1000);
    t.start();
  }

  /**
   * Interrupts all timer in the game.
   *
   * @param gameId the id for the game to interrupt.
   */
  public void dropAllTimer(String gameId) {
    if (threads.containsKey(gameId)) {
      for (Thread t : threads.get(gameId)) {
        t.interrupt();
        LOG.debug("Interrupted thread {} for game {}", t.getName(), gameId);
      }
    } else {
      LOG.warn("There is no thread for the gameId {}", gameId);
    }
  }
}
