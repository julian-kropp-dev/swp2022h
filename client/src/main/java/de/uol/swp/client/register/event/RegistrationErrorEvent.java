package de.uol.swp.client.register.event;

/**
 * Event used to show the RegistrationError alert.
 *
 * <p>In order to show the RegistrationError alert using this event, post an instance of it onto the
 * eventBus the SceneManager is subscribed to.
 *
 * @see de.uol.swp.client.SceneManager
 */
public class RegistrationErrorEvent {
  private final String message;

  /**
   * Constructor.
   *
   * @param message Message containing the cause of the Error
   */
  public RegistrationErrorEvent(String message) {
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
