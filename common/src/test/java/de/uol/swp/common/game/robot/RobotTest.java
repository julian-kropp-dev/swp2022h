package de.uol.swp.common.game.robot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.floor.Direction;
import de.uol.swp.common.game.floor.FloorField;
import de.uol.swp.common.game.floor.Library;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RobotTest {

  Robot robot;

  FloorField field_RP; // Robot Position

  ProgrammingCard[] cardArray =
      new ProgrammingCard[] {
        new ProgrammingCard(20, CardType.MOVE1),
        new ProgrammingCard(20, CardType.MOVE2),
        new ProgrammingCard(20, CardType.MOVE3),
        new ProgrammingCard(20, CardType.TURN_RIGHT),
        new ProgrammingCard(20, CardType.TURN_LEFT),
        new ProgrammingCard(20, CardType.BACKUP),
        new ProgrammingCard(20, CardType.U_TURN),
        new ProgrammingCard(20, CardType.TURN_LEFT),
        new ProgrammingCard(20, CardType.TURN_RIGHT),
      };

  //
  //	BasicFloorFieldIDs
  //
  String EMPTY_FIELD = "00";

  @BeforeEach
  void setup() {
    field_RP = new FloorField(Direction.NORTH, Library.get(EMPTY_FIELD), 0, 0);
    robot = new Robot(Robots.BOB, field_RP);
  }

  // -------------------------------------------------------------------------------

  @Test
  void newRobotTest() {
    assertEquals(Robots.BOB, robot.getType());
    assertEquals(field_RP, robot.getPosition());
    assertEquals(Direction.EAST, robot.getDirection());
    assertTrue(robot.isAlive());
    assertEquals(3, robot.getLives());
    assertEquals(0, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    assertEquals(Robot.MEMORY_SIZE, robot.getHandCards().length);
    assertEquals(Robot.PROGRAM_SIZE, robot.getSelectedCards().length);
    assertEquals(0, robot.getCurrentCheckpoint());
    assertFalse(robot.isLastMoveByBelt());
    assertFalse(robot.isNeedGuiInteraction());
  }

  @Test
  void setHandCardsTest() {
    robot.setHandCards(cardArray);
    assertHandCardsUptoSlot(Robot.MEMORY_SIZE);
  }

  @Test
  void damageTest() {
    robot.damage();
    robot.setHandCards(cardArray);
    assertHandCardsUptoSlot(Robot.MEMORY_SIZE - 1);

    robot.destroy();
    assertFalse(robot.isAlive());
    assertEquals(2, robot.getLives());
    robot.alive();
    robot.setHandCards(cardArray);
    assertHandCardsUptoSlot(Robot.MEMORY_SIZE);

    robot.destroy();
    assertFalse(robot.isAlive());
    assertEquals(1, robot.getLives());
    robot.alive();
    for (int i = 0; i < Robot.MEMORY_SIZE; i++) {
      robot.damage();
    }
    assertEquals(9, robot.getDamage());
    assertTrue(robot.isAlive());
    assertEquals(1, robot.getLives());
    robot.setHandCards(cardArray);
    assertHandCardsUptoSlot(0);
    assertArrayEquals(new boolean[] {true, true, true, true, true}, robot.getBlocked());

    robot.damage();
    assertFalse(robot.isAlive());
    assertEquals(0, robot.getLives());
  }

  @Test
  void repairTest() {
    int hiddenSlots = Robot.MEMORY_SIZE - Robot.PROGRAM_SIZE;
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    for (int i = 1; i <= hiddenSlots; i++) {
      robot.damage();
      assertEquals(i, robot.getDamage());
      assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    }
    robot.damage();
    assertEquals(hiddenSlots + 1, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, false, true}, robot.getBlocked());
    robot.damage();
    assertEquals(hiddenSlots + 2, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, true, true}, robot.getBlocked());
    //
    assertEquals(1, robot.repair(1), "1. Repair of 1 damage");
    assertEquals(hiddenSlots + 1, robot.getDamage());
    robot.unblockSlot(3);
    assertArrayEquals(new boolean[] {false, false, false, false, true}, robot.getBlocked());
    //
    assertEquals(1, robot.repair(1), "2. Repair of 1 damage");
    assertEquals(hiddenSlots, robot.getDamage());
    robot.unblockSlot(4);
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    //
    assertEquals(0, robot.repair(1), "3. Repair: nothing to unblock");
    assertEquals(hiddenSlots - 1, robot.getDamage());
  }

  // -------------------------------------------------------------------------------
  // onStartRespawnInteractionInternalMessage
  // -------------------------------------------------------------------------------

  @Test
  void shouldAcceptCardSelection() {
    assertEquals(0, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    ProgrammingCard[] selectedCards;
    //
    ProgrammingCard[] hardCodedHand = {
      new ProgrammingCard(1, CardType.MOVE1),
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(3, CardType.MOVE3),
      new ProgrammingCard(4, CardType.TURN_LEFT),
      new ProgrammingCard(5, CardType.TURN_RIGHT),
      new ProgrammingCard(6, CardType.U_TURN),
      new ProgrammingCard(7, CardType.BACKUP),
      new ProgrammingCard(8, CardType.TURN_LEFT),
      new ProgrammingCard(9, CardType.TURN_RIGHT),
    };
    robot.setHandCards(hardCodedHand);
    //
    // first 5 cards
    selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(1, CardType.MOVE1),
          new ProgrammingCard(2, CardType.MOVE2),
          new ProgrammingCard(3, CardType.MOVE3),
          new ProgrammingCard(4, CardType.TURN_LEFT),
          new ProgrammingCard(5, CardType.TURN_RIGHT),
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "Update successful");
    // different 5 cards
    selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(5, CardType.TURN_RIGHT),
          new ProgrammingCard(6, CardType.U_TURN),
          new ProgrammingCard(7, CardType.BACKUP),
          new ProgrammingCard(8, CardType.TURN_LEFT),
          new ProgrammingCard(9, CardType.TURN_RIGHT),
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "Update successful");
    // selected, reordered 5 cards
    selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(7, CardType.BACKUP),
          new ProgrammingCard(2, CardType.MOVE2),
          new ProgrammingCard(3, CardType.MOVE3),
          new ProgrammingCard(9, CardType.TURN_RIGHT),
          new ProgrammingCard(5, CardType.TURN_RIGHT),
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "Update successful");
    //
    // less than 5 valid cards
    selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(7, CardType.BACKUP),
          new ProgrammingCard(2, CardType.MOVE2),
          new ProgrammingCard(3, CardType.MOVE3),
          new ProgrammingCard(9, CardType.TURN_RIGHT),
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    //
    selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(3, CardType.MOVE3),
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertEquals(selectedCards[0], robot.getSelectedCards()[0], "Update successful");
    //
    assertTrue(robot.updateSelectedCards(new ProgrammingCard[0]));
    //
    // less than 5 valid cards with NULL
    assertTrue(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(7, CardType.BACKUP),
              new ProgrammingCard(2, CardType.MOVE2),
              new ProgrammingCard(3, CardType.MOVE3),
              new ProgrammingCard(9, CardType.TURN_RIGHT),
              null
            }),
        "Update valid");
    assertTrue(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(7, CardType.BACKUP),
              null,
              new ProgrammingCard(2, CardType.MOVE2),
              new ProgrammingCard(3, CardType.MOVE3),
              new ProgrammingCard(9, CardType.TURN_RIGHT)
            }),
        "Update valid");
    //
    selectedCards = new ProgrammingCard[] {null, new ProgrammingCard(3, CardType.MOVE3), null};
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertEquals(selectedCards[1], robot.getSelectedCards()[1], "Update successful");
    //
    assertTrue(robot.updateSelectedCards(new ProgrammingCard[] {null, null, null, null, null}));
  }

  @Test
  void shouldRejectCardSelection() {
    assertEquals(0, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    ProgrammingCard[] oldSelectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(5, CardType.TURN_RIGHT),
          new ProgrammingCard(6, CardType.U_TURN),
          new ProgrammingCard(7, CardType.BACKUP),
          new ProgrammingCard(8, CardType.TURN_LEFT),
          new ProgrammingCard(9, CardType.TURN_RIGHT),
        };
    robot.setSelectedCards(oldSelectedCards);
    //
    ProgrammingCard[] hardCodedHand = {
      new ProgrammingCard(1, CardType.MOVE1),
      new ProgrammingCard(2, CardType.MOVE2),
      new ProgrammingCard(3, CardType.MOVE3),
      new ProgrammingCard(4, CardType.TURN_LEFT),
      new ProgrammingCard(5, CardType.TURN_RIGHT),
      new ProgrammingCard(6, CardType.U_TURN),
      new ProgrammingCard(7, CardType.BACKUP),
      new ProgrammingCard(8, CardType.TURN_LEFT),
      new ProgrammingCard(9, CardType.TURN_RIGHT),
    };
    robot.setHandCards(hardCodedHand);
    //
    // one cheating card
    assertFalse(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(1, CardType.MOVE1),
              new ProgrammingCard(2, CardType.MOVE2),
              new ProgrammingCard(10, CardType.TURN_LEFT), // cheat
              new ProgrammingCard(4, CardType.TURN_LEFT),
              new ProgrammingCard(5, CardType.TURN_RIGHT),
            }));
    assertArrayEquals(oldSelectedCards, robot.getSelectedCards(), "Update unsuccessful");
    //
    // one double card
    assertFalse(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(1, CardType.MOVE1),
              new ProgrammingCard(2, CardType.MOVE2),
              new ProgrammingCard(4, CardType.TURN_LEFT),
              new ProgrammingCard(4, CardType.TURN_LEFT), // cheat
              new ProgrammingCard(5, CardType.TURN_RIGHT),
            }));
    assertArrayEquals(oldSelectedCards, robot.getSelectedCards(), "Update unsuccessful");
    //
    // more than 5 cards
    assertFalse(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(1, CardType.MOVE1),
              new ProgrammingCard(2, CardType.MOVE2),
              new ProgrammingCard(6, CardType.U_TURN),
              new ProgrammingCard(7, CardType.BACKUP),
              new ProgrammingCard(4, CardType.TURN_LEFT),
              new ProgrammingCard(5, CardType.TURN_RIGHT),
            }));
    assertArrayEquals(oldSelectedCards, robot.getSelectedCards(), "Update unsuccessful");
  }

  @Test
  void updateSelectedCardsTest_withBlockedSlots() {
    assertEquals(0, robot.getDamage());
    assertArrayEquals(new boolean[] {false, false, false, false, false}, robot.getBlocked());
    robot.setSelectedCards(
        new ProgrammingCard[] {
          new ProgrammingCard(0, CardType.BACKUP),
          new ProgrammingCard(0, CardType.BACKUP),
          new ProgrammingCard(4, CardType.MOVE2),
          new ProgrammingCard(0, CardType.BACKUP),
          new ProgrammingCard(5, CardType.MOVE3),
        });
    robot.setBlocked(new boolean[] {false, false, true, false, true});
    assertArrayEquals(new boolean[] {false, false, true, false, true}, robot.getBlocked());
    //
    ProgrammingCard[] player1HardCodedHand = {
      new ProgrammingCard(1, CardType.TURN_LEFT),
      new ProgrammingCard(3, CardType.MOVE1),
      new ProgrammingCard(6, CardType.U_TURN),
    };
    robot.setHandCards(player1HardCodedHand);
    //
    // 3 valid cards in unblocked slots
    // and identical ones in blocked slots
    ProgrammingCard[] selectedCards =
        new ProgrammingCard[] {
          new ProgrammingCard(3, CardType.MOVE1),
          new ProgrammingCard(6, CardType.U_TURN),
          new ProgrammingCard(4, CardType.MOVE2), // blocked
          new ProgrammingCard(1, CardType.TURN_LEFT),
          new ProgrammingCard(5, CardType.MOVE3), // blocked
        };
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "Update successful");
    //
    // 3 valid cards in unblocked slots
    // but NOT identical ones in blocked slots
    assertFalse(
        robot.updateSelectedCards(
            new ProgrammingCard[] {
              new ProgrammingCard(3, CardType.MOVE1),
              new ProgrammingCard(6, CardType.U_TURN),
              new ProgrammingCard(7, CardType.BACKUP), // blocked, cheat
              new ProgrammingCard(1, CardType.TURN_LEFT),
              new ProgrammingCard(5, CardType.MOVE3), // blocked
            }));
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "Update unsuccessful");
    //
    // all slots blocked, empty hand
    robot.setBlocked(new boolean[] {true, true, true, true, true});
    assertArrayEquals(new boolean[] {true, true, true, true, true}, robot.getBlocked());
    robot.setHandCards(new ProgrammingCard[0]);
    //
    assertTrue(robot.updateSelectedCards(selectedCards), "Update valid");
    assertArrayEquals(selectedCards, robot.getSelectedCards(), "no change");
  }

  // -------------------------------------------------------------------------------
  // private helper methods
  // -------------------------------------------------------------------------------

  private void assertHandCardsUptoSlot(int undamaged) {
    for (int i = 0; i < Robot.MEMORY_SIZE; i++) {
      ProgrammingCard card = robot.getHandCards()[i];
      if (i < undamaged) {
        assertNotNull(card);
        assertEquals(cardArray[i].getCardType(), card.getCardType());
      } else {
        assertNull(card);
      }
    }
  }
}
