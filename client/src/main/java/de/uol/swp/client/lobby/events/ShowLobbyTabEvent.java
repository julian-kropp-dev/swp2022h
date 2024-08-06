package de.uol.swp.client.lobby.events;

import javafx.scene.control.Tab;

/**
 * Event used to show LobbyView.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class ShowLobbyTabEvent {
  private final String lobbyId;
  private final Tab tab;
  private final String lobbyTitle;

  /**
   * ShowLobbyTabEvent for the Eventbus.
   *
   * @param lobbyId TabName
   * @param lobbyTitle LobbyTitle
   * @param tab Tab
   */
  public ShowLobbyTabEvent(String lobbyId, String lobbyTitle, Tab tab) {
    this.lobbyId = lobbyId;
    this.tab = tab;
    this.lobbyTitle = lobbyTitle;
  }

  public String getLobbyId() {
    return lobbyId;
  }

  public Tab getTab() {
    return tab;
  }

  public String getLobbyTitle() {
    return lobbyTitle;
  }
}
