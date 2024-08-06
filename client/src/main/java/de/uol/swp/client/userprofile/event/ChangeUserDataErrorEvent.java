package de.uol.swp.client.userprofile.event;

/** Event used to indicate that an Error occurred while changing data of a user. */
public class ChangeUserDataErrorEvent {
  private final String message;

  /**
   * Constructor.
   *
   * @param message Message containing the cause of the Error
   */
  public ChangeUserDataErrorEvent(String message) {
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
