package de.uol.swp.common.user.userdata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The class UserData stores information about the User.
 *
 * <p>Stores the avatar id and the statistics, if available
 */
public class UserData implements Serializable {
  private static final long serialVersionUID = -6507265127864991052L;
  private final int avatarId;
  private final List<UserStatistic> userStatistics;

  /**
   * Default constructor.
   *
   * @param avatarId the avatar id as an integer
   * @param userStatistics the user statistics Object from the User
   */
  public UserData(int avatarId, List<UserStatistic> userStatistics) {
    if (avatarId < 0) {
      throw new IllegalArgumentException("placement cant be lower than zero");
    }
    this.avatarId = avatarId;
    this.userStatistics = userStatistics;
  }

  /**
   * Constructor for only avatar id, when the userStatistics can be null.
   *
   * @param avatarId the avatar id as an integer
   */
  public UserData(int avatarId) {
    this(avatarId, new ArrayList<>());
  }

  /**
   * Getter for the Avatar id.
   *
   * @return the avatar id from a user as an int
   */
  public int getAvatarId() {
    return avatarId;
  }

  /**
   * Getter for the user statistics.
   *
   * @return the user statistics from a user as an UserStatistics object
   */
  public List<UserStatistic> getUserStatistics() {
    return userStatistics;
  }
}
