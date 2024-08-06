package de.uol.swp.server.game.ai;

import com.google.common.eventbus.EventBus;
import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.game.floor.FloorPlan;
import de.uol.swp.common.game.player.Player;

/**
 * The abstract AI class represents an AI player in the game. It implements the runnable interface,
 * allowing it to be executed as a separate thread.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class Ai implements Runnable {

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  protected GameDTO gameDTO;

  protected Player player;

  protected EventBus bus;

  protected FloorPlan floorPlan;

  /** Constructs an Ai instance. */
  protected Ai() {}

  /**
   * Sets the data required for the Ai player.
   *
   * @param gameDTO The game data transfer object.
   * @param player The player associated with the Ai.
   * @param bus The event bus for communication between components.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public void setData(GameDTO gameDTO, Player player, EventBus bus) {
    this.gameDTO = gameDTO;
    this.player = player;
    this.bus = bus;
    this.floorPlan = gameDTO.getFloorPlan();
  }
}
