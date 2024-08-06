package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * Request to update an user.
 *
 * @see de.uol.swp.common.user.User
 */
public class UpdateUserRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 3603378307253264040L;
  private final User toUpdate;
  private final String password;

  /**
   * Constructor.
   *
   * @param user the user object the sender shall be updated to unchanged fields being empty
   */
  public UpdateUserRequest(User user, String password) {
    this.toUpdate = user;
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
    UpdateUserRequest that = (UpdateUserRequest) o;
    return Objects.equals(toUpdate, that.toUpdate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(toUpdate);
  }
}
