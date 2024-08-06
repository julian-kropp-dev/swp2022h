package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import de.uol.swp.common.user.User;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A request message to initiate the shutdown of a robot. Extends the AbstractRequestMessage class.
 */
public class ShutdownRobotRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 408954920834092L;
  private final String gameId;
  private final User user;

  /**
   * Constructor for the ShutdownRobotRequest.
   *
   * @param gameId the Game in which the robot wants to shut down his robot;
   * @param user the user who wants to shutdown his Robot;
   */
  public ShutdownRobotRequest(@Nonnull String gameId, User user) {
    this.gameId = gameId;
    this.user = user;
  }

  /**
   * Getter for the GameID.
   *
   * @return the GameId of the Game from which the request originated;
   */
  public String getGameId() {
    return gameId;
  }

  /**
   * Getter for the user.
   *
   * @return the User from which the the request to shutdown his robot came
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
    ShutdownRobotRequest that = (ShutdownRobotRequest) o;
    return Objects.equals(gameId, that.gameId) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), gameId, user);
  }
}
