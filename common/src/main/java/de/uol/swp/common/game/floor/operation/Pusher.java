package de.uol.swp.common.game.floor.operation;

import de.uol.swp.common.game.floor.Operation;
import java.util.Arrays;
import java.util.List;

/**
 * The Pusher class is a helper class for the Pusher operation in a game's floor.
 *
 * <p>The Pusher operation represents a type of tile on a game board where a player can push on a
 * neighbor field. When a player lands on a Pusher tile, the execute() method of this class is
 * called to perform the Pit operation.
 */
public class Pusher extends Operation {

  private final List<Integer> programSteps;

  public Pusher(Integer[] programmStep) {
    this.programSteps = Arrays.asList(programmStep);
  }

  public Pusher(int programmStep) {
    this.programSteps = List.of(programmStep);
  }

  public boolean isInProgramStep(Integer programStep) {
    return programSteps.contains(programStep);
  }
}
