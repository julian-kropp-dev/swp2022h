package de.uol.swp.common.game.floor.operation;

import de.uol.swp.common.game.floor.Operation;

/** Repair operation class. */
public class Repair extends Operation {
  private final int number;

  public Repair(int number) {
    this.number = number;
  }

  public int getNumber() {
    return number;
  }
}
