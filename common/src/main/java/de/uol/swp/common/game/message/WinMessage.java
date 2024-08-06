package de.uol.swp.common.game.message;

import de.uol.swp.common.game.Game;
import de.uol.swp.common.game.player.Player;
import de.uol.swp.common.message.AbstractServerMessage;

/** Message that displays the winner of a game. */
public class WinMessage extends AbstractServerMessage {
  private final Game game;
  private final Player winner;

  public WinMessage(Game game, Player winner) {
    this.game = game;
    this.winner = winner;
  }

  /**
   * getter for Game.
   *
   * @return the game of the winner
   */
  public Game getGame() {
    return game;
  }

  /**
   * Getter for winner.
   *
   * @return the winner of the played game.
   */
  public Player getWinner() {
    return winner;
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
