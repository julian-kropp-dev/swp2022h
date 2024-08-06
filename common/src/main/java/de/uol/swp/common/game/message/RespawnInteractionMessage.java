package de.uol.swp.common.game.message;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.user.User;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Message that the Server need a Respawn with a Direction and a new field of the Robot. */
public class RespawnInteractionMessage extends AbstractServerMessage {
  private static final long serialVersionUID = -5187922561582039L;
  private final User user;
  private final Integer[][] fields;
  private final int time;
  private final Direction direction;
  private final String gameId;

  /**
   * Constructs a RespawnInteractionMessage.
   *
   * @param user the user associated with the interaction
   * @param fields the fields involved in the interaction
   * @param direction the direction of the respawn
   * @param time the time of the interaction
   */
  public RespawnInteractionMessage(
      @Nonnull User user,
      @Nonnull Integer[][] fields,
      @Nonnull Direction direction,
      int time,
      String gameId) {
    this.user = user;
    this.fields = fields;
    this.time = time;
    this.direction = direction;
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  /**
   * Retrieves the user associated with the interaction.
   *
   * @return the user associated with the interaction
   */
  public User getUser() {
    return user;
  }

  /**
   * Retrieves the fields involved in the interaction.
   *
   * @return the fields involved in the interaction
   */
  public Integer[][] getFields() {
    return fields;
  }

  /**
   * Retrieves the direction of the respawn.
   *
   * @return the direction of the respawn
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Retrieves the time of the interaction.
   *
   * @return the time of the interaction
   */
  public int getTime() {
    return time;
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
    RespawnInteractionMessage that = (RespawnInteractionMessage) o;
    return time == that.time
        && user.equals(that.user)
        && Arrays.deepEquals(fields, that.fields)
        && direction == that.direction;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(super.hashCode(), user, time, direction);
    result = 31 * result + Arrays.deepHashCode(fields);
    return result;
  }
}
