package de.uol.swp.common.game.animation;

import de.uol.swp.common.game.floor.Direction;

/** The different types of animations. */
public enum AnimationType {

  // Movement
  WALL_NORTH,
  WALL_EAST,
  WALL_SOUTH,
  WALL_WEST,

  MOVED_NORTH,
  MOVED_EAST,
  MOVED_SOUTH,
  MOVED_WEST,

  MOVED_NORTH_BY_BELT,
  MOVED_EAST_BY_BELT,
  MOVED_SOUTH_BY_BELT,
  MOVED_WEST_BY_BELT,

  // Animation without driving
  TURN_LEFT,
  TURN_RIGHT,
  U_TURN,
  NO_TURN,

  // Animation without movement
  PIT,
  GAME_OVER,
  WIN,
  ARCHIVE,
  CHECKPOINT,
  REPAIR,
  LASER_DAMAGE,
  DESTROYED,
  PRESS,

  // TYPES FOR DISPLAYING WHAT IS HAPPENING
  MOVEMENT,
  FACTORY_PHASE_START,
  FACTORY_PHASE_ROUND_TWO,
  FACTORY_PHASE_ROUND_THREE,
  FACTORY_PHASE_ROUND_FOUR,
  FACTORY_PHASE_ROUND_FIVE,
  FACTORY_PHASE_EXPRESS_ONE,
  FACTORY_PHASE_EXPRESS_TWO_AND_NORMAL,
  FACTORY_PHASE_PUSHER1,
  FACTORY_PHASE_PUSHER2,
  FACTORY_PHASE_PUSHER3,
  FACTORY_PHASE_PUSHER4,
  FACTORY_PHASE_PUSHER5,
  FACTORY_PHASE_GEARS,
  FACTORY_PHASE_GEARS1,
  FACTORY_PHASE_GEARS2,
  FACTORY_PHASE_GEARS3,
  FACTORY_PHASE_GEARS4,
  FACTORY_PHASE_GEARS5,
  FACTORY_PHASE_PRESS1,
  FACTORY_PHASE_PRESS2,
  FACTORY_PHASE_PRESS3,
  FACTORY_PHASE_PRESS4,
  FACTORY_PHASE_PRESS5,
  FACTORY_PHASE_LASER,
  FACTORY_PHASE_ROBOT_LASER,
  DISPLAY_NEXT_CARD,
  UPDATE_DAMAGE_AND_LIVES,
  REPAIR_FIELD;

  private static final String ILLEGAL_ARGUMENT_STRING = "Not a valid state: ";

  /**
   * Get a AnimationType for a Turn Direction.
   *
   * @param direction direction
   * @return AnimationType
   */
  public static AnimationType getAnimationForTurning(Direction direction) {
    switch (direction) {
      case WEST:
        return TURN_LEFT;
      case EAST:
        return TURN_RIGHT;
      case SOUTH:
        return U_TURN;
      default:
        return NO_TURN;
    }
  }

  /**
   * This method calculates the AnimationType for a turn, based on the start and target direction.
   *
   * @param start Start direction
   * @param target Target direction
   * @return AnimationType
   */
  public static AnimationType calculateTurningDir(Direction start, Direction target) {
    if (start.ordinal() > target.ordinal()) {
      return TURN_LEFT;
    }
    if (start.ordinal() < target.ordinal()) {
      return TURN_RIGHT;
    }
    return null;
  }

  /**
   * Get a AnimationType for a Belt Animation.
   *
   * @param direction direction
   * @return AnimationType
   */
  public static AnimationType getAnimationForBelt(Direction direction) {
    switch (direction) {
      case NORTH:
        return MOVED_NORTH_BY_BELT;
      case EAST:
        return MOVED_EAST_BY_BELT;
      case SOUTH:
        return MOVED_SOUTH_BY_BELT;
      default:
        return MOVED_WEST_BY_BELT;
    }
  }

  /**
   * Get a AnimationType for a Wall Animation.
   *
   * @param direction direction
   * @return AnimationType
   */
  public static AnimationType getAnimationForWall(Direction direction) {
    switch (direction) {
      case NORTH:
        return WALL_NORTH;
      case EAST:
        return WALL_EAST;
      case SOUTH:
        return WALL_SOUTH;
      default:
        return WALL_WEST;
    }
  }

  /**
   * Get a AnimationType for a Movement.
   *
   * @param direction direction
   * @return AnimationType
   */
  public static AnimationType getAnimationForDriving(Direction direction) {
    switch (direction) {
      case NORTH:
        return MOVED_NORTH;
      case EAST:
        return MOVED_EAST;
      case SOUTH:
        return MOVED_SOUTH;
      default:
        return MOVED_WEST;
    }
  }

  /**
   * Get a AnimationType for a Factory Phase.
   *
   * @param phase number of phase
   * @return AnimationType
   */
  public static AnimationType getAnimationForFactoryPhase(int phase) {
    switch (phase) {
      case 0:
        return FACTORY_PHASE_START;
      case 1:
        return FACTORY_PHASE_ROUND_TWO;
      case 2:
        return FACTORY_PHASE_ROUND_THREE;
      case 3:
        return FACTORY_PHASE_ROUND_FOUR;
      case 4:
        return FACTORY_PHASE_ROUND_FIVE;
      default:
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT_STRING + phase);
    }
  }

  /**
   * Get a AnimationType for a Factory Phase.
   *
   * @param phase number of phase
   * @return AnimationType
   */
  public static AnimationType getAnimationTypeForPusher(int phase) {
    switch (phase) {
      case 0:
        return FACTORY_PHASE_PUSHER1;
      case 1:
        return FACTORY_PHASE_PUSHER2;
      case 2:
        return FACTORY_PHASE_PUSHER3;
      case 3:
        return FACTORY_PHASE_PUSHER4;
      case 4:
        return FACTORY_PHASE_PUSHER5;
      default:
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT_STRING + phase);
    }
  }

  /**
   * Get a AnimationType for a Factory Phase.
   *
   * @param phase number of phase
   * @return AnimationType
   */
  public static AnimationType getAnimationTypeForPress(int phase) {
    switch (phase) {
      case 0:
        return FACTORY_PHASE_PRESS1;
      case 1:
        return FACTORY_PHASE_PRESS2;
      case 2:
        return FACTORY_PHASE_PRESS3;
      case 3:
        return FACTORY_PHASE_PRESS4;
      case 4:
        return FACTORY_PHASE_PRESS5;
      default:
        throw new IllegalArgumentException(ILLEGAL_ARGUMENT_STRING + phase);
    }
  }
}
