package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a request message to update user statistics. This request includes the user object and
 * the user statistic to be updated.
 */
public class RetrieveUserStatsRequest extends AbstractRequestMessage {
  private static final long serialVersionUID = 36033783076654040L;
  private final User user;

  public RetrieveUserStatsRequest(@NonNull User user) {
    this.user = user;
  }

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
    RetrieveUserStatsRequest that = (RetrieveUserStatsRequest) o;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), user);
  }
}
