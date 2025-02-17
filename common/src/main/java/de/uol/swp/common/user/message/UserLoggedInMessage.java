package de.uol.swp.common.user.message;

import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/**
 * A message to indicate a newly logged-in user.
 *
 * <p>This message is used to automatically update the user lists of every connected client as soon
 * as a user successfully logs in
 */
public class UserLoggedInMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -2071886836547126480L;
  private String username;

  /**
   * Default constructor.
   *
   * @implNote Do not use for valid login since no username gets set
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public UserLoggedInMessage() {}

  /**
   * Constructor.
   *
   * @param username the username of the newly logged-in user
   */
  public UserLoggedInMessage(String username) {
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
    UserLoggedInMessage that = (UserLoggedInMessage) o;
    return Objects.equals(username, that.username);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }
}
