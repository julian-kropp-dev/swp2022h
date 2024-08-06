package de.uol.swp.common.game.player;

/** An enumeration of the different types of players and AIs levels in the game. */
public enum PlayerType {
  HUMAN_PLAYER,
  SPECTATOR,
  AI_PLAYER_EASY,
  AI_PLAYER_MEDIUM,
  AI_PLAYER_HARD,
  AI_PLAYER_GODLIKE;

  /**
   * Return the PlayerType from an integer.
   *
   * @param intValue the value for the PlayerType
   * @return the actual Enum
   */
  public static PlayerType fromInt(int intValue) {
    for (PlayerType type : PlayerType.values()) {
      if (type.ordinal() == intValue) {
        return type;
      }
    }
    return null;
  }
}
