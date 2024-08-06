package de.uol.swp.common.user.message;

import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/**
 * A message to indicate a user is logged out.
 *
 * <p>This message is used to automatically update the user lists of every connected client as soon
 * as a user successfully logs out
 */
public class UserLoggedOutMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -6156329809066290863L;
  private String username;

  /**
   * Default constructor.
   *
   * @implNote Do not use for valid logout since no username gets set
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public UserLoggedOutMessage() {}

  /**
   * Constructor.
   *
   * @param username the username of the newly logged-out user
   */
  public UserLoggedOutMessage(String username) {
    this.username = username;
  }

  /**
   * Getter for the username.
   *
   * @return String containing the username
   */
  public String getUsername() {
    return username;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserLoggedOutMessage that = (UserLoggedOutMessage) o;
    return Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
