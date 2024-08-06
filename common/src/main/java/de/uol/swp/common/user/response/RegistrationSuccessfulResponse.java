package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * A response, that the user registration was successful.
 *
 * <p>This response is only sent to clients that previously sent a RegisterUserRequest that was
 * executed successfully, otherwise an ExceptionMessage would be sent.
 */
public class RegistrationSuccessfulResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -1219784918011150685L;
  private final User user;

  /**
   * Constructor.
   *
   * @param user the user who successfully logged in
   */
  public RegistrationSuccessfulResponse(User user) {
    this.user = user;
  }

  /**
   * Getter for the User variable.
   *
   * @return User object of the user who successfully logged in
   */
  public User getUser() {
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegistrationSuccessfulResponse that = (RegistrationSuccessfulResponse) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
