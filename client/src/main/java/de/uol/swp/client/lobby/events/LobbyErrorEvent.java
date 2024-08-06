package de.uol.swp.client.lobby.events;

/**
 * Event used to show the LobbyError alert
 *
 * <p>In order to show the LobbyError alert using this event, post an instance of it onto the
 * eventBus the SceneManager is subscribed to.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class LobbyErrorEvent {
  private final String message;

  /**
   * Constructor.
   *
   * @param message Message containing the cause of the Error
   */
  public LobbyErrorEvent(String message) {
    this.message = message;
  }

  /**
   * Gets the error message.
   *
   * @return A String containing the error message
   */
  public String getMessage() {
    return message;
  }
}
