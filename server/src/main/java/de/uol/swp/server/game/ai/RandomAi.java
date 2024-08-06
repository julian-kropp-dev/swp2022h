package de.uol.swp.server.game.ai;

import de.uol.swp.common.game.card.CardType;
import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.request.ValidateCardSelectionRequest;
import de.uol.swp.server.game.GameService;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * This class represents a random Ai player that extends the base class 'Ai'. It overrides the
 * 'run()' method to provide its own implementation.
 */
@SuppressWarnings("UnstableApiUsage")
public class RandomAi extends Ai {
  ProgrammingCard[] cards;

  public RandomAi(ProgrammingCard[] cards) {
    this.cards = cards;
  }

  /**
   * Executes the logic for the random Ai player. It retrieves the programming cards of the player's
   * robot and shuffles them, ensuring that the first card is a move card (MOVE1, MOVE2, or MOVE3).
   * The shuffled cards are then assigned back to the player's robot.
   *
   * @throws RuntimeException if the logic encounters an error after 5 iterations.
   */
  @Override
  @SuppressWarnings({"java:S112", "java:S3776"})
  public void run() {
    ProgrammingCard[] programmingCards = cards;
    ProgrammingCard[] programmingCards1;
    boolean[] blocked = player.getRobot().getBlocked();
    boolean forceSort = false;
    int i = 0;
    do {
      i++;
      if (i >= 100) {
        forceSort = true;
      }

      if (i >= 110) {
        throw new RuntimeException("Error in the Ai");
      }
      programmingCards1 = Mix.mix(programmingCards, blocked);
      // Ai Level >= 3, sort the card to move first in the first round
      if (player.getPlayerType().ordinal() >= 3 && gameDTO.isInFirstRoundOfTheGame()) {
        gameDTO.setInFirstRoundOfTheGame(false);
        programmingCards1 =
            Arrays.stream(programmingCards1)
                .min(Comparator.comparingInt(p -> p.getCardType().ordinal()))
                .stream()
                .toArray(ProgrammingCard[]::new);
      }
      // Ai Level >= 2, then place first element to a move card
      if (player.getPlayerType().ordinal() >= 2 && gameDTO.isInFirstRoundOfTheGame() || forceSort) {
        programmingCards1 = Mix.firstCardMove(programmingCards1);
        if (programmingCards1.length == 0) {
          throw new RuntimeException("Error in the Ai");
        }
      }
    } while (!(programmingCards1[0].getCardType() == CardType.MOVE1
            || programmingCards1[0].getCardType() == CardType.MOVE2
            || programmingCards1[0].getCardType() == CardType.MOVE3)
        && gameDTO.isInFirstRoundOfTheGame());
    ProgrammingCard[] selectedCards =
        Arrays.stream(programmingCards1).limit(5).toArray(ProgrammingCard[]::new);

    if (calculateRandomValue(player.getUser().getUUID(), 20) == 12) {
      GameService.getInstance().sendAiMessage(gameDTO.getGameId(), player, false);
    }
    bus.post(
        new ValidateCardSelectionRequest(
            player.getUser().getUsername(), gameDTO.getGameId(), selectedCards));
  }

  @SuppressWarnings("SameParameterValue")
  private int calculateRandomValue(String uuid, int maxExclusiveInt) {
    int randomValue = 0;
    for (char value : uuid.toCharArray()) {
      randomValue = randomValue + value;
    }

    Random random = new Random(System.currentTimeMillis() + randomValue);
    return random.nextInt(maxExclusiveInt);
  }
}
