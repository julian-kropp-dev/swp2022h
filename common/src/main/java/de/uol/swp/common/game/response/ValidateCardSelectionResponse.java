package de.uol.swp.common.game.response;

import de.uol.swp.common.game.card.ProgrammingCard;
import java.util.Arrays;

/**
 * Servers response of a ValidateCardSelectionRequest containing requestId, gameId, userName/userId
 * and a success flag.
 */
public class ValidateCardSelectionResponse extends AbstractGameResponse {

  private final ProgrammingCard[] selectedCards;

  public ValidateCardSelectionResponse(
      String requestId,
      String gameId,
      String userName,
      ProgrammingCard[] selectedCards,
      boolean success) {
    super(requestId, gameId, userName, success);
    this.selectedCards = selectedCards;
  }

  public ProgrammingCard[] getSelectedCards() {
    return selectedCards;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ValidateCardSelectionResponse that = (ValidateCardSelectionResponse) o;
    return Arrays.equals(selectedCards, that.selectedCards);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(selectedCards);
    return result;
  }
}
