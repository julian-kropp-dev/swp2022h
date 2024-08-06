package de.uol.swp.common.game.floor.operation;

import de.uol.swp.common.game.floor.Operation;

/** Checkpoint operation class. */
public class Checkpoint extends Operation {

  private final int number;

  public Checkpoint(int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }
}
