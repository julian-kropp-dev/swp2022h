package de.uol.swp.common.game.floor;

import java.io.Serializable;

/**
 * Class for the FloorPlanSettings that comes from the client. It is separate to the other FloorPlan
 * class, so the client doesn't always have to create a fully generated FloorPlan, when updating the
 * LobbyOptions.
 */
public class FloorPlanSetting implements Serializable {

  public static final int FLOOR_PLAN_SIZE = 12;

  private static final long serialVersionUID = -1619931959264812575L;
  private FloorPlans floorPlans = FloorPlans.EMPTY;
  private Direction direction;

  /**
   * Private default constructor, so there can't be an instance without setting a FloorPlans Enum.
   */
  @SuppressWarnings("unused")
  private FloorPlanSetting() {
    // can't be an instance without setting a FloorPlans Enum.
  }

  /** Constructor of FloorPlanSetting that sets FloorPlans enum with default Direction NORTH. */
  public FloorPlanSetting(FloorPlans floorPlans) {
    this(floorPlans, Direction.NORTH);
  }

  /** Constructor of FloorPlanSetting that sets both enums at the same time. */
  public FloorPlanSetting(FloorPlans floorPlans, Direction direction) {
    this.floorPlans = floorPlans;
    this.direction = direction;
  }

  /** Getter for the FloorPlans enum. */
  public FloorPlans getFloorPlansEnum() {
    return floorPlans;
  }

  /**
   * Getter for direction.
   *
   * @return direction of the FloorPlan
   */
  public Direction getDirection() {
    return direction;
  }

  /**
   * Setter for the direction.
   *
   * @param direction of the FloorPlan
   */
  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  /**
   * Enumeration for the 6 different FloorFlans. The EMPTY is here because a field on the selection
   * (Drag&Drop) area can also be empty, depending on the used client.
   */
  public enum FloorPlans {
    CROSS,
    EXCHANGE,
    ISLAND,
    MAELSTROM,
    CANNERY_ROW,
    PIT_MAZE,
    EMPTY
  }
}
