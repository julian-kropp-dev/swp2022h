package de.uol.swp.server.game.ai;

import de.uol.swp.common.game.card.ProgrammingCard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This class provides a method for shuffling programming cards based on a list of blocked cards.
 */
public class Mix {

  private Mix() {}

  /**
   * Shuffles the programming cards based on the list of blocked cards. Only non-blocked cards are
   * considered for shuffling.
   *
   * @param programmingCards The array of programming cards to be shuffled.
   * @param blocked The array indicating which cards are blocked.
   * @return The shuffled array of programming cards.
   */
  public static ProgrammingCard[] mix(ProgrammingCard[] programmingCards, boolean[] blocked) {
    List<Integer> list;
    list = new ArrayList<>();
    for (int i = 0; i < programmingCards.length; i++) {
      if (i < blocked.length && !blocked[i]) {
        list.add(i);
      }
      if (i >= blocked.length) {
        list.add(i);
      }
    }
    Integer[] arrayList1 = list.toArray(new Integer[0]);
    Integer[] arrayList2 = list.toArray(new Integer[0]);

    Integer tmp;
    int rand;
    Random r = new Random(System.currentTimeMillis());
    for (int i = 0; i < arrayList2.length; i++) {
      rand = r.nextInt(arrayList2.length);
      tmp = arrayList2[i];
      arrayList2[i] = arrayList2[rand];
      arrayList2[rand] = tmp;
    }
    ProgrammingCard[] programmingCards1;
    programmingCards1 = programmingCards.clone();
    for (int i = 0; i < arrayList1.length; i++) {
      programmingCards1[arrayList1[i]] = programmingCards[arrayList2[i]];
    }
    programmingCards = programmingCards1.clone();
    return programmingCards;
  }

  /**
   * Switch the programming-cards, that the first card is a move card.
   *
   * @param programmingCards1 input cards
   * @return switch cards
   */
  public static ProgrammingCard[] firstCardMove(ProgrammingCard[] programmingCards1) {
    Optional<ProgrammingCard> firstCardOptional =
        Arrays.stream(programmingCards1)
            .min(Comparator.comparingInt(p -> p.getCardType().ordinal()));

    if (firstCardOptional.isEmpty()) {
      return new ProgrammingCard[0];
    }

    ProgrammingCard firstCard = firstCardOptional.get();
    for (int j = 0; j < programmingCards1.length; j++) {
      ProgrammingCard card = programmingCards1[j];
      if (card.equals(firstCard)) {
        ProgrammingCard switchCard = programmingCards1[0];
        programmingCards1[0] = firstCard;
        programmingCards1[j] = switchCard;
      }
    }
    return programmingCards1;
  }
}
