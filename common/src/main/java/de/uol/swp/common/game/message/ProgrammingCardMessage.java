package de.uol.swp.common.game.message;

import de.uol.swp.common.game.card.ProgrammingCard;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.message.AbstractServerMessage;
import de.uol.swp.common.user.User;
import java.util.Map;
import java.util.NoSuchElementException;

/** Class for sending Programming Cards to alle Players in a Game. */
public class ProgrammingCardMessage extends AbstractServerMessage {

  private static final long serialVersionUID = -7574348326619251826L;
  private final String gameId;
  private final Map<Player, ProgrammingCard[]> cards;
  private final Map<Player, boolean[]> blockedSlots;

  /**
   * Constructor for the ProgrammingCardMessage.
   *
   * @param cards Player-ProgrammingCard[]-Map: Containing all cards for all players
   * @param gameId ID of the Game
   * @param blockedSlots Broken Slots of the Players Robot
   */
  public ProgrammingCardMessage(
      Map<Player, ProgrammingCard[]> cards, String gameId, Map<Player, boolean[]> blockedSlots) {
    this.cards = cards;
    this.gameId = gameId;
    this.blockedSlots = blockedSlots;
  }

  public ProgrammingCard[] getCards(Player player) {
    return cards.get(player);
  }

  public boolean[] getBlocked(Player player) {
    return blockedSlots.get(player);
  }

  public String getGameId() {
    return gameId;
  }

  /**
   * Retrieves the Player associated with the specified User.
   *
   * @param user the User to find the Player for
   * @return the Player associated with the User
   * @throws NoSuchElementException if the User does not exist in the ProgrammingCardMessage
   */
  public Player getPlayer(User user) throws NoSuchElementException {
    for (Player player : cards.keySet()) {
      if (player.getUser().equals(user)) {
        return player;
      }
    }
    throw new NoSuchElementException("This User does not exits in this ProgrammingCardMessage");
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
