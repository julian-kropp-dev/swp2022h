package de.uol.swp.common.game.request;

import de.uol.swp.common.message.AbstractRequestMessage;

/** Request for the GameDTO. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class GameDTORequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 5503985843674827592L;
  private final String gameId;

  public GameDTORequest(String gameId) {
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
