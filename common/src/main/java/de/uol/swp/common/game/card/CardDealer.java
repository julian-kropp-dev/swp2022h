package de.uol.swp.common.game.card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Card Dealer for Programming Cards.
 *
 * @see de.uol.swp.common.game.card.ProgrammingCard
 */
public class CardDealer {

  private static final Logger LOG = LogManager.getLogger(CardDealer.class);
  private static final String TEXT = "Number of cards needed is higher than cards available";
  private final Deque<ProgrammingCard> cardStackMoveCards = new ArrayDeque<>();
  private final Deque<ProgrammingCard> cardStackTurnCards = new ArrayDeque<>();
  private final Deque<ProgrammingCard> allCardStack = new ArrayDeque<>();
  private final List<ProgrammingCard> freshDeckMoveCards;
  private final List<ProgrammingCard> freshDeckTurnCards;

  /**
   * Constructor.
   *
   * @param freshDeckMoveCards List of all move Cards
   * @param freshDeckTurnCards List of all turn Cards
   */
  public CardDealer(
      List<ProgrammingCard> freshDeckMoveCards, List<ProgrammingCard> freshDeckTurnCards) {
    Collections.shuffle(freshDeckMoveCards);
    Collections.shuffle(freshDeckTurnCards);
    this.freshDeckMoveCards = freshDeckMoveCards;
    this.freshDeckTurnCards = freshDeckTurnCards;

    cardStackMoveCards.addAll(freshDeckMoveCards);
    cardStackTurnCards.addAll(freshDeckTurnCards);
  }

  /**
   * CardDealer.
   *
   * @param damagePoints Integer between 0 and 10
   * @return ProgrammingCards as an Array
   */
  public ProgrammingCard[][] dealCards(int[] damagePoints, List<ProgrammingCard> cardsAlreadyInUse)
      throws IllegalArgumentException, IllegalStateException {
    refresh(cardsAlreadyInUse);
    int numberOfPlayers = damagePoints.length;
    ProgrammingCard[][] returnCards = new ProgrammingCard[numberOfPlayers][];

    if (Arrays.stream(damagePoints).filter(value -> value < 0 || value > 10).count() != 0) {
      throw new IllegalArgumentException("DamagePoints must be between 0 and 10");
    }
    LOG.trace(
        "Generating Cards for {} player. Needing in total {} cards",
        numberOfPlayers,
        numberOfPlayers * 9 - Arrays.stream(damagePoints).sum());

    // first card is a move card
    for (int i = 0; i < numberOfPlayers; i++) {
      int numberOfCards = 9 - damagePoints[i];
      if (numberOfCards < 0) {
        numberOfCards = 0;
      }
      if (numberOfCards > getRemainingCards()) {
        LOG.trace(
            "Number of Cards needed {} and Number of Cards remaining {}",
            numberOfCards,
            getRemainingCards());
        throw new IllegalStateException(TEXT);
      }
      returnCards[i] = new ProgrammingCard[numberOfCards];
      firstCardIsMoveCard(numberOfCards, returnCards[i]);
    }
    combineDecks();
    // random cards
    for (int i = 0; i < numberOfPlayers; i++) {
      int numberOfCards = 9 - damagePoints[i];
      if (numberOfCards < 0) {
        numberOfCards = 0;
      }
      if (numberOfCards > getRemainingCards()) {
        LOG.trace(
            "Number of Cards needed {} and Number of Cards remaining {}",
            numberOfCards,
            getRemainingCards());
        throw new IllegalStateException(TEXT);
      }
      getCardsForRemainingSlots(numberOfCards, returnCards[i]);
    }
    return returnCards;
  }

  /** Picks a random move card for the first slots. */
  private void firstCardIsMoveCard(
      int numberOfCards,
      ProgrammingCard[] returnCards) { // reference returnCards does not need to be returned
    if (numberOfCards > 0) {
      try {
        returnCards[0] = cardStackMoveCards.pop();
      } catch (NoSuchElementException e) {
        LOG.fatal("Card stack is empty");
        throw new IllegalStateException(TEXT);
      }
    }
  }

  /** Combines the cardStackMoveCards and cardStackTurnCards in one allCardStack. */
  private void combineDecks() {
    List<ProgrammingCard> allCards = new ArrayList<>();
    allCards.addAll(cardStackMoveCards);
    allCards.addAll(cardStackTurnCards);
    Collections.shuffle(allCards);
    allCardStack.addAll(allCards);
  }

  /** Picks a random turn or move card for the remaining slots. */
  private void getCardsForRemainingSlots(
      int numberOfCards,
      ProgrammingCard[] returnCards) { // reference returnCards does not need to be returned
    if (numberOfCards <= 1) {
      return;
    }
    for (int i = 1; i < numberOfCards; i++) {
      try {
        returnCards[i] = allCardStack.pop();
      } catch (NoSuchElementException e) {
        LOG.fatal("Card stack is empty");
        throw new IllegalStateException(TEXT);
      }
    }
  }

  private int getRemainingCards() {
    return cardStackMoveCards.size() + cardStackTurnCards.size();
  }

  private void refresh(List<ProgrammingCard> cardsAlreadyInUse) {
    allCardStack.clear();
    cardStackMoveCards.clear();
    cardStackTurnCards.clear();

    Collections.shuffle(freshDeckMoveCards);
    Collections.shuffle(freshDeckTurnCards);

    List<ProgrammingCard> tempMove = new ArrayList<>(freshDeckMoveCards);
    tempMove.removeAll(cardsAlreadyInUse);

    List<ProgrammingCard> tempTurn = new ArrayList<>(freshDeckTurnCards);
    tempTurn.removeAll(cardsAlreadyInUse);

    cardStackMoveCards.addAll(tempMove);
    cardStackTurnCards.addAll(tempTurn);
    LOG.trace("Refreshed Card Stack");
  }
}
