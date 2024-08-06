package de.uol.swp.server.message;

import de.uol.swp.common.user.User;
import java.util.Objects;

/**
 * This message is used if a successful logout occurred.
 *
 * <p>This message is used to signalize all Services it is relevant to, that someone just logged out
 * successfully
 *
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.usermanagement.AuthenticationService
 */
public class ClientLoggedOutMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 3260161005857884466L;
  private final User user;

  /**
   * Constructor.
   *
   * @param user user whose client logged out successfully
   * @see de.uol.swp.common.user.User
   */
  public ClientLoggedOutMessage(User user) {
    super();
    this.user = user;
  }

  /**
   * Getter for the user attribute.
   *
   * @return the user whose client logged out successfully
   * @see de.uol.swp.common.user.User
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
    if (!super.equals(o)) {
      return false;
    }
    ClientLoggedOutMessage that = (ClientLoggedOutMessage) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), user);
  }
}
