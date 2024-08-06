package de.uol.swp.common.game.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardDealerTest {
  /** Test the ProgrammingCard Object and the Constructor of the Object. */
  CardDealer dealer =
      new CardDealer(
          ProgrammingCardManager.getProgramingCardSetMove(),
          ProgrammingCardManager.getProgramingCardSetTurn());

  List<ProgrammingCard> cardsAlreadyInUse = new ArrayList<>();

  @BeforeEach
  void resetCardDealer() {
    dealer =
        new CardDealer(
            ProgrammingCardManager.getProgramingCardSetMove(),
            ProgrammingCardManager.getProgramingCardSetTurn());
  }

  @Test
  void cardTestException() {
    assertThrows(
        IllegalArgumentException.class, () -> dealer.dealCards(new int[] {-1}, cardsAlreadyInUse));
    assertThrows(
        IllegalArgumentException.class, () -> dealer.dealCards(new int[] {11}, cardsAlreadyInUse));
    assertThrows(
        IllegalStateException.class,
        () -> dealer.dealCards(new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 5}, cardsAlreadyInUse));
  }

  @Test
  void cardTestAmountOneRobot() {
    assertEquals(9, dealer.dealCards(new int[] {0, 0}, cardsAlreadyInUse)[0].length);
    assertEquals(8, dealer.dealCards(new int[] {1}, cardsAlreadyInUse)[0].length);
    assertEquals(7, dealer.dealCards(new int[] {2}, cardsAlreadyInUse)[0].length);
    assertEquals(6, dealer.dealCards(new int[] {3}, cardsAlreadyInUse)[0].length);
    assertEquals(5, dealer.dealCards(new int[] {4}, cardsAlreadyInUse)[0].length);
    assertEquals(4, dealer.dealCards(new int[] {5}, cardsAlreadyInUse)[0].length);
    assertEquals(3, dealer.dealCards(new int[] {6}, cardsAlreadyInUse)[0].length);
    assertEquals(2, dealer.dealCards(new int[] {7}, cardsAlreadyInUse)[0].length);
    assertEquals(1, dealer.dealCards(new int[] {8}, cardsAlreadyInUse)[0].length);
    assertEquals(0, dealer.dealCards(new int[] {9}, cardsAlreadyInUse)[0].length);
    assertEquals(0, dealer.dealCards(new int[] {10}, cardsAlreadyInUse)[0].length);
  }

  @Test
  void cardTestAmountMultipleRobots() {
    for (int robots = 1; robots <= 6; robots++) { // robots
      for (int damage = 0; damage <= 9; damage++) { // damage
        int[] array = new int[robots];
        Arrays.fill(array, damage);
        ProgrammingCard[][] cards = dealer.dealCards(array, cardsAlreadyInUse);
        for (ProgrammingCard[] programmingCards : cards) {
          assertTrue(includesMoveCard(programmingCards));
        }
        assertEquals(robots * (9 - damage), reduce2DArray(cards).size());
      }
    }
  }

  @Test
  void cardTestMultiplePlayersAmount() {
    ProgrammingCard[][] cards = dealer.dealCards(new int[] {0, 1, 2, 3}, cardsAlreadyInUse);

    assertEquals(9, cards[0].length);
    assertEquals(8, cards[1].length);
    assertEquals(7, cards[2].length);
    assertEquals(6, cards[3].length);
  }

  @Test
  void cardTestUniqueCards() {
    ProgrammingCard[][] cards =
        dealer.dealCards(new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 6}, cardsAlreadyInUse);
    assertEquals(0, findDuplicate(reduce2DArray(cards)).size());
  }

  @Test
  void atLeastOneMoveCard() {
    for (int i = 0; i < 75000; i++) {
      ProgrammingCard[][] cards = dealer.dealCards(new int[] {0, 0, 0, 0, 0, 0}, cardsAlreadyInUse);
      for (ProgrammingCard[] programmingCards : cards) {
        assertTrue(includesMoveCard(programmingCards));
      }
    }
  }

  @Test
  void CardsAlreadyInUse() {
    // 84-42 = 42
    List<ProgrammingCard> blockedCards =
        new ArrayList<>(ProgrammingCardManager.getProgramingCardSetTurn()); // size 42;
    ProgrammingCard[][] cards = dealer.dealCards(new int[] {0, 0, 0, 0, 3}, blockedCards);
    assertEquals(42, reduce2DArray(cards).size());
    assertEquals(0, findDuplicate(reduce2DArray(cards)).size());

    for (ProgrammingCard[] programmingCards : cards) {
      assertTrue(includesMoveCard(programmingCards));
    }

    assertThrows(
        IllegalStateException.class,
        () -> dealer.dealCards(new int[] {0, 0, 0, 0, 2}, blockedCards));
    assertThrows(
        IllegalStateException.class,
        () -> dealer.dealCards(new int[] {0, 0, 0, 0, 1}, blockedCards));
  }

  private Set<ProgrammingCard> findDuplicate(List<ProgrammingCard> list) {
    Set<Integer> items = new HashSet<>();
    return list.stream().filter(e -> !items.add(e.getCount())).collect(Collectors.toSet());
  }

  private List<ProgrammingCard> reduce2DArray(ProgrammingCard[][] cards) {
    List<ProgrammingCard> allCards = new ArrayList<>();
    for (ProgrammingCard[] cardsOfOnePlayer : cards) {
      allCards.addAll(List.of(cardsOfOnePlayer));
    }
    return allCards;
  }

  private boolean includesMoveCard(ProgrammingCard[] cards) {
    if (cards.length == 0) {
      return true;
    }
    for (ProgrammingCard card : cards) {
      if (card.isMoveCard()) {
        return true;
      }
    }
    return false;
  }
}
