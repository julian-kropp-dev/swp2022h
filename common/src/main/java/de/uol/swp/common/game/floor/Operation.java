package de.uol.swp.common.game.floor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** This is the super class of all operation kinds. */
public abstract class Operation implements Serializable {

  private static final long serialVersionUID = 3672874341488621049L;
  private List<Direction> directions = new ArrayList<>();

  /**
   * Getter for the directions of the operation.
   *
   * @return list of directions
   */
  public List<Direction> getDirections() {
    return directions;
  }

  /**
   * Setter for the directions of a operation.
   *
   * @param directions list of directions
   */
  public void setDirections(List<Direction> directions) {
    this.directions = directions;
  }
}
