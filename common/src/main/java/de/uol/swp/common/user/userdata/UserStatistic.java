package de.uol.swp.common.user.userdata;

import java.io.Serializable;
import java.time.LocalDateTime;

/** The class UserStatistic store a statistic from the User. */
public class UserStatistic implements Serializable {

  private static final long serialVersionUID = -6507265127864914152L;
  private final int placement;
  private final LocalDateTime dateTime;

  /**
   * default constructor.
   *
   * @param placement the placement position of a game
   * @param dateTime the time from the game end
   */
  public UserStatistic(int placement, LocalDateTime dateTime) {
    if (placement < 0) {
      throw new IllegalArgumentException("placement cant be lower than zero");
    }
    this.placement = placement;
    this.dateTime = dateTime;
  }

  /**
   * Getter for the placement.
   *
   * @return the placement from a user as an int
   */
  public int getPlacement() {
    return placement;
  }

  /**
   * Getter for the time of the game.
   *
   * @return the time from the entry.
   */
  public LocalDateTime getDateTime() {
    return dateTime;
  }
}
