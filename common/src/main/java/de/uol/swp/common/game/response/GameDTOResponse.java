package de.uol.swp.common.game.response;

import de.uol.swp.common.game.dto.GameDTO;
import de.uol.swp.common.message.AbstractResponseMessage;

/** Response for the GameDTORequest. */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class GameDTOResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = 572406910654192216L;

  private final GameDTO gameDTO;

  public GameDTOResponse(GameDTO gameDTO) {
    this.gameDTO = gameDTO;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public GameDTO getGameDTO() {
    return gameDTO;
  }
}
