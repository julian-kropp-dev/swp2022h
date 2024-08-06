package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * Request to register a new user.
 *
 * @see de.uol.swp.common.user.User
 */
public class RegisterUserRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = -1687276257530110813L;
  private final User toCreate;

  /**
   * Constructor.
   *
   * @param user the new User to create
   */
  public RegisterUserRequest(User user) {
    this.toCreate = user;
  }

  @Override
  public boolean authorizationNeeded() {
    return false;
  }

  /**
   * Getter for the user variable.
   *
   * @return the new user to create
   */
  public User getUser() {
    return toCreate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegisterUserRequest that = (RegisterUserRequest) o;
    return Objects.equals(toCreate, that.toCreate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(toCreate);
  }
}
