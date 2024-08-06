package de.uol.swp.server.message;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a request message to update user statistics. This request includes the user object and
 * the user statistic to be updated.
 */
public class UpdateUserStatsMessage extends AbstractRequestMessage {
  private static final long serialVersionUID = 3603323457876654040L;
  private final User user;
  private final UserStatistic userStatistic;

  public UpdateUserStatsMessage(@NonNull User user, @NonNull UserStatistic userStatistic) {
    this.user = user;
    this.userStatistic = userStatistic;
  }

  public User getUser() {
    return user;
  }

  public UserStatistic getUserStatistic() {
    return userStatistic;
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
    UpdateUserStatsMessage that = (UpdateUserStatsMessage) o;
    return Objects.equals(user, that.user) && Objects.equals(userStatistic, that.userStatistic);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), user, userStatistic);
  }
}
