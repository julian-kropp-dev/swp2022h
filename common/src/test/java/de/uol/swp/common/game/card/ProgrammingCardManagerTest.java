package de.uol.swp.common.game.card;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProgrammingCardManagerTest {

  List<ProgrammingCard> cardsMove = ProgrammingCardManager.getProgramingCardSetMove();
  List<ProgrammingCard> cardsTurn = ProgrammingCardManager.getProgramingCardSetTurn();

  @Test
  void cardTestSize() {
    assertEquals(84, cardsMove.size() + cardsTurn.size());
    assertEquals(18, cardsMove.stream().filter(e -> e.getCardType() == CardType.MOVE1).count());
    assertEquals(12, cardsMove.stream().filter(e -> e.getCardType() == CardType.MOVE2).count());
    assertEquals(6, cardsMove.stream().filter(e -> e.getCardType() == CardType.MOVE3).count());
    assertEquals(
        18, cardsTurn.stream().filter(e -> e.getCardType() == CardType.TURN_RIGHT).count());
    assertEquals(18, cardsTurn.stream().filter(e -> e.getCardType() == CardType.TURN_LEFT).count());
    assertEquals(6, cardsTurn.stream().filter(e -> e.getCardType() == CardType.U_TURN).count());
    assertEquals(6, cardsMove.stream().filter(e -> e.getCardType() == CardType.BACKUP).count());
  }
}
