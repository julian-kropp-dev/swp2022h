package de.uol.swp.client.game.events;

import javafx.scene.control.Tab;

/**
 * Event to show the GameTab.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class ShowGameTabEvent {

  private final Tab tabToClose;
  private final String lobbyId;

  /**
   * Constructor to set the tabToClose.
   *
   * @param tabToClose tab that should be closed
   * @param lobbyId the id of the lobby that belongs to the game
   */
  public ShowGameTabEvent(Tab tabToClose, String lobbyId) {
    this.tabToClose = tabToClose;
    this.lobbyId = lobbyId;
  }

  /**
   * Getter for tabToClose.
   *
   * @return the tab to close
   */
  public Tab getTabToClose() {
    return tabToClose;
  }

  /**
   * Getter for the lobbyId.
   *
   * @return the lobbyId
   */
  public String getLobbyId() {
    return lobbyId;
  }
}
