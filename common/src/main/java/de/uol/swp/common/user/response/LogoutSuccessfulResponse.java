package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * A message containing the session of the logged-out user.
 *
 * <p>This response is sent to the Client whose LogoutRequest was successful
 *
 * @see de.uol.swp.common.user.request.LogoutRequest
 * @see User
 */
public class LogoutSuccessfulResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -1219784918011050685L;
  private final User user;

  /**
   * Constructor.
   *
   * @param user the user who successfully logged out
   */
  public LogoutSuccessfulResponse(User user) {
    this.user = user;
  }

  /**
   * Getter for the User variable.
   *
   * @return User object of the user who successfully logged out
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
    LogoutSuccessfulResponse that = (LogoutSuccessfulResponse) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user);
  }
}
