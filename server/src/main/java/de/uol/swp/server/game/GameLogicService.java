package de.uol.swp.server.game;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.Game;
import de.uol.swp.server.ServerConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The GameLogicService class represents a service for managing the game logic.
 *
 * <p>It provides functionality for starting the game logic for a given game by creating a new
 * thread
 *
 * <p>for executing the GameLogic object.
 */
@SuppressWarnings("UnstableApiUsage")
public class GameLogicService {

  private static final Logger LOG = LogManager.getLogger(GameLogicService.class);
  private final ServerConfig serverConfig = ServerConfig.getInstance();
  private final ThreadPoolExecutor executor =
      (ThreadPoolExecutor) Executors.newFixedThreadPool(serverConfig.getThreadCount());
  private final GameService gameService;
  private final EventBus bus;
  private final Map<String, Future<?>> map = new HashMap<>();
  private final boolean testMode;

  /**
   * Default constructor.
   *
   * @param bus The server's bus.
   */
  public GameLogicService(GameService gameService, EventBus bus) {
    this(gameService, bus, false);
  }

  /**
   * Constructor.
   *
   * @param bus The server's bus.
   * @param testMode true for UnitTests
   */
  public GameLogicService(GameService gameService, EventBus bus, boolean testMode) {
    this.bus = bus;
    this.gameService = gameService;
    this.testMode = testMode;
  }

  /**
   * start the thread for the logic.
   *
   * @param game the game to start
   */
  public void startLogic(Game game) {
    GameLogic gameLogic = new GameLogic(game, gameService, bus, testMode);
    map.put(game.getGameId(), executor.submit(gameLogic));
  }

  /**
   * interrupt the thread for the logic.
   *
   * @param gameId the id of the game
   */
  public void stopLogic(String gameId) {
    if (map.containsKey(gameId)) {
      map.get(gameId).cancel(true);
      LOG.debug("Interrupted logic for {}", gameId);
    }
  }
}
