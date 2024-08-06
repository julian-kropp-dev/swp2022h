package de.uol.swp.common.user.response;

import de.uol.swp.common.message.AbstractResponseMessage;
import de.uol.swp.common.user.userdata.UserStatistic;
import java.util.List;
import java.util.Objects;

/**
 * Represents a response message indicating the successful update of user statistics. This response
 * contains the updated user object.
 */
public class UpdateUserStatsResponse extends AbstractResponseMessage {
  private static final long serialVersionUID = 117726543115596L;
  private final List<UserStatistic> userStatistic;

  public UpdateUserStatsResponse(List<UserStatistic> userStatistic) {
    this.userStatistic = userStatistic;
  }

  public List<UserStatistic> getUserStatistics() {
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
    UpdateUserStatsResponse that = (UpdateUserStatsResponse) o;
    return Objects.equals(userStatistic, that.userStatistic);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), userStatistic);
  }
}
