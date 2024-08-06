package de.uol.swp.common.game;

import static org.junit.jupiter.api.Assertions.*;

import de.uol.swp.common.game.GameOptions.GamePhase;
import org.junit.jupiter.api.Test;

/** Tests the GameOptions class to ensure its methods work as expected. */
class GameOptionsTest {

  private final GameOptions gameOptions = new GameOptions();

  /**
   * Tests the GameOptions constructor by ensuring it sets the game title and game phase correctly.
   */
  @Test
  void GameOptionsConstructorTest() {
    assertEquals(GamePhase.UPGRADE_PHASE, gameOptions.getGamePhase());
  }

  /** Tests the getGamePhase method by ensuring it returns the correct game phase. */
  @Test
  void getGamePhaseTest() {
    assertEquals(GamePhase.UPGRADE_PHASE, gameOptions.getGamePhase());
  }

  /**
   * Tests the setGamePhase method by ensuring it correctly sets the game phase and returns the
   * correct game phase.
   */
  @Test
  void setGamePhaseTest() {
    assertEquals(GamePhase.UPGRADE_PHASE, gameOptions.getGamePhase());
    gameOptions.setGamePhase(GamePhase.PROGRAMMING_PHASE);
    assertEquals(GamePhase.PROGRAMMING_PHASE, gameOptions.getGamePhase());
  }

  /**
   * Tests the isLastPhase method by ensuring it returns false when called with the current game
   * phase and true when called with the last game phase.
   */
  @Test
  void isLastPhaseTest() {
    assertFalse(gameOptions.isLastPhase(gameOptions.getGamePhase()));
    gameOptions.setGamePhase(GamePhase.VERIFICATION_PHASE);
    assertTrue(gameOptions.isLastPhase(gameOptions.getGamePhase()));
  }

  /**
   * Tests the nextPhase method by ensuring it correctly advances the game phase to the next phase.
   */
  @Test
  void nextPhaseTest() {
    gameOptions.nextPhase();
    assertEquals(GamePhase.PROGRAMMING_PHASE, gameOptions.getGamePhase());
    gameOptions.setGamePhase(GamePhase.ROBOT_MOVEMENT_PHASE);
    gameOptions.nextPhase();
    assertEquals(GamePhase.VERIFICATION_PHASE, gameOptions.getGamePhase());
  }
}
