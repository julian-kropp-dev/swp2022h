package de.uol.swp.common.game.message;

import de.uol.swp.common.message.AbstractServerMessage;

/** Message that Server wait to start a next round. */
public class WaitForNextRoundMessage extends AbstractServerMessage {
  private static final long serialVersionUID = -5187984111582039L;
  private final String gameId;

  /** Utility Message. */
  public WaitForNextRoundMessage() {
    /* This is a Utility Message, with no parameters */
    this.gameId = "";
  }

  public WaitForNextRoundMessage(String gameId) {
    this.gameId = gameId;
  }

  public String getGameId() {
    return gameId;
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
