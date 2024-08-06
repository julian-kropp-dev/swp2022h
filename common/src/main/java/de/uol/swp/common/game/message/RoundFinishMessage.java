package de.uol.swp.common.game.message;

import de.uol.swp.common.game.dto.Step;
import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/** Message that the GameLogic finished. */
public class RoundFinishMessage extends AbstractServerMessage {
  private static final long serialVersionUID = -5187984561582039L;
  private final Step step;

  private final String gameId;

  public RoundFinishMessage(Step step) {
    this.step = step;
    this.gameId = "";
  }

  public RoundFinishMessage(Step step, String gameId) {
    this.step = step;
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
  }

  /**
   * Returns the Step object representing the step that has been processed by the game logic.
   *
   * @return The Step object representing the step that has been processed by the game logic
   */
  public Step getStep() {
    return step;
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
    RoundFinishMessage that = (RoundFinishMessage) o;
    return Objects.equals(step, that.step);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), step);
  }
}
