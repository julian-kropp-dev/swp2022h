package de.uol.swp.client.game.events;

import javafx.scene.control.Tab;

/**
 * Event to show the WinnerTab.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class ShowWinnerTabEvent {
  private final String username;
  private final String lobbyId;
  private final Tab tabToClose;

  /**
   * Constructs a ShowWinnerTabEvent with the specified username, tab to close, and lobby ID.
   *
   * @param username the username associated with the event
   * @param lobbyId the ID of the lobby where the event occurred
   */
  public ShowWinnerTabEvent(String username, String lobbyId, Tab tabToClose) {
    this.username = username;
    this.lobbyId = lobbyId;
    this.tabToClose = tabToClose;
  }

  /**
   * Getter for Username.
   *
   * @return the Username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Getter for the LobbyID.
   *
   * @return the LobbyID.
   */
  public String getLobbyId() {
    return lobbyId;
  }

  /**
   * Getter for tabToClose.
   *
   * @return the tab to close
   */
  public Tab getTabToClose() {
    return tabToClose;
  }
}
