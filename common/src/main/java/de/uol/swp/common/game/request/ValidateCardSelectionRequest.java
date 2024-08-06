package de.uol.swp.common.game.request;

import de.uol.swp.common.game.card.ProgrammingCard;
import java.util.Arrays;

/** Request for validating the selection of programming cards. */
public class ValidateCardSelectionRequest extends AbstractGameRequest {

  ProgrammingCard[] selectedCards;

  public ValidateCardSelectionRequest(
      String username, String lobbyId, ProgrammingCard[] selectedCards) {
    super(username, lobbyId, RequestType.CARD_SELECTION, System.currentTimeMillis());
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
    ValidateCardSelectionRequest that = (ValidateCardSelectionRequest) o;
    return Arrays.equals(selectedCards, that.selectedCards);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(selectedCards);
    return result;
  }
}
