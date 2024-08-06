package de.uol.swp.common.game.floor.operation;

import de.uol.swp.common.game.floor.Operation;

/** Laser operation class. */
public class Laser extends Operation {

  private final Integer intensity;
  private final boolean isSource;

  /**
   * Constructor for the LaserOperation.
   *
   * @param intensity the damage the Laser does.
   * @param isSource whether the Laser is a Source or not.
   */
  public Laser(int intensity, boolean isSource) {
    this.intensity = intensity;
    this.isSource = isSource;
  }

  public boolean isSource() {
    return isSource;
  }

  public Integer getIntensity() {
    return this.intensity;
  }
}
