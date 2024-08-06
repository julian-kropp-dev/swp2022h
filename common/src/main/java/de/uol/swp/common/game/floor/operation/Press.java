package de.uol.swp.common.game.floor.operation;

import de.uol.swp.common.game.floor.Operation;
import java.util.Arrays;
import java.util.List;

/**
 * The Press class is a subclass of the Operation class and represents a press operation on a floor
 * tile in a game. The press operation is defined by a list of program steps, which indicate the
 * sequence of steps required to activate the press operation on the floor tile.
 */
public class Press extends Operation {
  private final List<Integer> programSteps;

  public Press(Integer[] programmStep) {
    this.programSteps = Arrays.asList(programmStep);
  }

  public Press(int programmStep) {
    this.programSteps = List.of(programmStep);
  }

  public boolean isInProgramStep(Integer programStep) {
    return programSteps.contains(programStep);
  }
}
