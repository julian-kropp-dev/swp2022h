package de.uol.swp.server.game.ai;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.server.ServerConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/** The AiService class manages and starts Ai players in a game. */
@SuppressWarnings("UnstableApiUsage")
public class AiService {

  private final EventBus bus;
  private final ServerConfig serverConfig = ServerConfig.getInstance();
  private final ThreadPoolExecutor executor =
      (ThreadPoolExecutor) Executors.newFixedThreadPool(serverConfig.getAiThreadCount());

  /**
   * Constructs a AiService instance with the provided event bus.
   *
   * @param bus The event bus used for communication between components.
   */
  public AiService(EventBus bus) {
    this.bus = bus;
  }

  /**
   * Starts an Ai player in the game by providing the necessary data and submitting it for
   * execution.
   *
   * @param gameDTO The game data transfer object.
   * @param player The player associated with the Ai.
   * @param ai The Ai player instance.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void startAi(GameDTO gameDTO, Player player, Ai ai) {
    ai.setData(gameDTO, player, bus);
    executor.submit(ai);
  }
}
