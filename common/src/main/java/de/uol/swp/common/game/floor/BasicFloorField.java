package de.uol.swp.common.game.floor;

import de.uol.swp.common.game.floor.operation.Checkpoint;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class for a basic floor field. */
public class BasicFloorField implements Serializable {

  private static final long serialVersionUID = 763101860838164101L;
  private final List<Direction> possibleNeighbours;
  private final Map<Integer, Operation> operations;
  private final String id;

  /**
   * Constructor to create a new basic floor field.
   *
   * @param id the id of the basic floor field
   * @param possibleNeighbours list of its possible neighbours
   * @param operations list of operations for this field
   */
  public BasicFloorField(
      String id, List<Direction> possibleNeighbours, Map<Integer, Operation> operations) {
    this.id = id;
    this.possibleNeighbours = possibleNeighbours;
    this.operations = operations;
  }

  /**
   * Get all possible neighbours of this field.
   *
   * @return list of possible neighbours
   */
  public List<Direction> getPossibleNeighbours() {
    return possibleNeighbours;
  }

  /**
   * Get all operations done by this field.
   *
   * @return map of operations
   */
  public Map<Integer, Operation> getOperations() {
    return operations;
  }

  /**
   * Get the ID of the floor field specified by the library.
   *
   * @return the ID of this field
   */
  public String getId() {
    return id;
  }

  /**
   * Makes a checkpoint out of the current BasicFloorField with a specified number.
   *
   * @param i the number of the checkpoint
   * @return the new BasicFloorField including the checkpoint
   */
  public BasicFloorField makeCheckpoint(int i) {
    Map<Integer, Operation> map = new HashMap<>(Map.copyOf(operations));
    map.put(7, new Checkpoint(i));
    return new BasicFloorField(this.id, this.possibleNeighbours, map);
  }
}
