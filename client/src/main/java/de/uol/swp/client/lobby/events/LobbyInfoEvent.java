package de.uol.swp.client.lobby.events;

/**
 * Event used to show the LobbyInfo alert.
 *
 * <p>In order to show the LobbyInfo alert using this event, post an instance of it onto the
 * eventBus the SceneManager is subscribed to.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class LobbyInfoEvent {
  private final String message;

  /**
   * Constructor.
   *
   * @param message Message containing the cause of the Information.
   */
  public LobbyInfoEvent(String message) {
    this.message = message;
  }

  /**
   * Gets the information message.
   *
   * @return A String containing the information message.
   */
  public String getMessage() {
    return message;
  }
}
