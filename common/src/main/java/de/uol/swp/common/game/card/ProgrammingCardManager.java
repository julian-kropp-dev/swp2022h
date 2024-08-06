package de.uol.swp.common.game.card;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** CardManger for creating ProgrammingCards. */
public class ProgrammingCardManager {

  private static final Logger LOG = LogManager.getLogger(ProgrammingCardManager.class);

  private ProgrammingCardManager() {
    throw new IllegalStateException();
  }

  /**
   * Generate a Programming Card set for move cards.
   *
   * @return A list containing all cards for moving
   */
  public static List<ProgrammingCard> getProgramingCardSetMove() {
    List<ProgrammingCard> cards = new ArrayList<>();
    cards.addAll(generateList(490, 660, 10, CardType.MOVE1));
    cards.addAll(generateList(670, 780, 10, CardType.MOVE2));
    cards.addAll(generateList(790, 840, 10, CardType.MOVE3));
    cards.addAll(generateList(430, 480, 10, CardType.BACKUP));
    LOG.trace("Created ProgrammingCard Stack");
    return cards;
  }

  /**
   * Generate a Programming Card set for turn cards.
   *
   * @return A list containing all cards for turning
   */
  public static List<ProgrammingCard> getProgramingCardSetTurn() {
    List<ProgrammingCard> cards = new ArrayList<>();
    cards.addAll(generateList(80, 420, 20, CardType.TURN_RIGHT));
    cards.addAll(generateList(70, 410, 20, CardType.TURN_LEFT));
    cards.addAll(generateList(10, 60, 10, CardType.U_TURN));
    LOG.trace("Created ProgrammingCard Stack");
    return cards;
  }

  /**
   * Generates ProgrammingCards.
   *
   * @param start Start value
   * @param end End value
   * @param step distance between the ProgrammingCounter
   * @param type Enum CardType for RobotDirection
   * @return a list with created ProgrammingCards
   */
  private static ArrayList<ProgrammingCard> generateList(
      int start, int end, int step, CardType type) {
    ArrayList<ProgrammingCard> list = new ArrayList<>();
    for (int i = start; i <= end; i = i + step) {
      try {
        list.add(new ProgrammingCard(i, type));
      } catch (Exception e) {
        LOG.error("An Unexpected Error has occurred", e);
      }
    }
    return list;
  }
}
