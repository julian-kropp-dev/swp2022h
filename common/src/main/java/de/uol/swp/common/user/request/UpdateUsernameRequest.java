package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * Request to update an username.
 *
 * @see de.uol.swp.common.user.User
 */
public class UpdateUsernameRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 6971210969052815206L;
  private final User toUpdate;
  private final String newUsername;
  private final String password;

  /**
   * Constructor.
   *
   * @param user the current user to send to the Server
   * @param newUsername The new Username to send to the Server
   */
  public UpdateUsernameRequest(User user, String newUsername, String password) {
    this.toUpdate = user;
    this.newUsername = newUsername;
    this.password = password;
  }

  /**
   * Getter for the updated user object.
   *
   * @return the updated user object
   */
  public User getUser() {
    return toUpdate;
  }

  /**
   * Getter for the Username.
   *
   * @return the new Username
   */
  public String getUpdateName() {
    return newUsername;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateUsernameRequest that = (UpdateUsernameRequest) o;
    return (Objects.equals(toUpdate, that.toUpdate)
        && (Objects.equals(newUsername, that.newUsername)));
  }

  @Override
  public int hashCode() {
    return Objects.hash(toUpdate);
  }
}
