package de.uol.swp.common.game.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.robot.Robot;
import de.uol.swp.common.game.robot.Robots;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The ProgrammingCardTest class contains tests for the ProgrammingCard class. */
class ProgrammingCardTest {
  private ProgrammingCard card;
  private ProgrammingCard card2;
  private ProgrammingCard card4;
  private ProgrammingCard card5;
  private ProgrammingCard card6;
  private ProgrammingCard card7;
  private ProgrammingCard card8;
  private ProgrammingCard card9;
  private ProgrammingCard card10;
  private Robot owner;

  /** Initializes the test instance. */
  @BeforeEach
  void setUp() {
    card = new ProgrammingCard(2, CardType.MOVE1);
    card4 = new ProgrammingCard(4, CardType.MOVE2);
    card5 = new ProgrammingCard(5, CardType.MOVE3);
    card6 = new ProgrammingCard(6, CardType.BACKUP);
    card7 = new ProgrammingCard(7, CardType.TURN_LEFT);
    card8 = new ProgrammingCard(8, CardType.TURN_RIGHT);
    card9 = new ProgrammingCard(9, CardType.U_TURN);
    owner = new Robot(Robots.DUSTY, null);
  }

  /** Tests the getOwner() method. */
  @Test
  void testGetOwner() {
    assertNull(card.getOwner());
    card.setOwner(owner.getType());
    assertEquals(owner.getType(), card.getOwner());
    card.setOwner(null);
    assertNull(card.getOwner());
  }

  /** Tests the getCount() method. */
  @Test
  void testGetCount() {
    assertEquals(2, card.getCount());
    assertEquals(4, card4.getCount());
    assertEquals(5, card5.getCount());
    assertEquals(6, card6.getCount());
    assertEquals(7, card7.getCount());
    assertEquals(8, card8.getCount());
    assertEquals(9, card9.getCount());
    assertThrows(IllegalArgumentException.class, () -> new ProgrammingCard(-42, CardType.U_TURN));
  }

  /** Tests the getCardType() method. */
  @Test
  void testGetCardType() {
    assertEquals(CardType.MOVE1, card.getCardType());
    assertThrows(IllegalArgumentException.class, () -> card2 = new ProgrammingCard(3, null));
    assertNull(card2);
    ProgrammingCard card3 = new ProgrammingCard(3, CardType.MOVE1);
    assertNotNull(card3);
    assertEquals(CardType.MOVE2, card4.getCardType());
    assertEquals(CardType.MOVE3, card5.getCardType());
    assertEquals(CardType.BACKUP, card6.getCardType());
    assertEquals(CardType.TURN_LEFT, card7.getCardType());
    assertEquals(CardType.TURN_RIGHT, card8.getCardType());
    assertEquals(CardType.U_TURN, card9.getCardType());
  }

  /** Tests the getDirection() method. */
  @Test
  void testGetDirection() {
    assertEquals(Direction.NORTH, card.getDirection());
    assertEquals(Direction.NORTH, card4.getDirection());
    assertEquals(Direction.NORTH, card5.getDirection());
    assertEquals(Direction.NORTH, card6.getDirection());
    assertEquals(Direction.WEST, card7.getDirection());
    assertEquals(Direction.EAST, card8.getDirection());
    assertEquals(Direction.SOUTH, card9.getDirection());
  }

  /** Tests the getSteps() method. */
  @Test
  void testGetSteps() {
    assertEquals(1, card.getSteps());
    assertEquals(2, card4.getSteps());
    assertEquals(3, card5.getSteps());
    assertEquals(-1, card6.getSteps());
    assertEquals(0, card7.getSteps());
    assertEquals(0, card8.getSteps());
    assertEquals(0, card9.getSteps());
    assertThrows(IllegalArgumentException.class, () -> card10 = new ProgrammingCard(30, null));
    assertThrows(NullPointerException.class, () -> card10.getSteps());
  }

  /** Tests the isMoveCardMethod */
  @Test
  void testIsMoveCard() {
    assertTrue(card.isMoveCard());
    assertTrue(card4.isMoveCard());
    assertTrue(card5.isMoveCard());
    assertTrue(card6.isMoveCard());
    assertFalse(card7.isMoveCard());
    assertFalse(card8.isMoveCard());
    assertFalse(card9.isMoveCard());
  }
}
