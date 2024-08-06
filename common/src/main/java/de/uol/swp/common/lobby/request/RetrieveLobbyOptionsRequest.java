package de.uol.swp.common.lobby.request;

import de.uol.swp.common.message.AbstractRequestMessage;
import java.util.Objects;

/** Class for RetrieveLobbyOptionsRequest. */
public class RetrieveLobbyOptionsRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 5391497142631888639L;

  private final String lobbyId;

  /**
   * Constructor to set the lobbyId.
   *
   * @param lobbyId of the lobby which wants to retrieve the options
   */
  public RetrieveLobbyOptionsRequest(String lobbyId) {
    this.lobbyId = lobbyId;
  }

  /**
   * Getter for the lobbyId.
   *
   * @return the lobbyId
   */
  public String getLobbyId() {
    return lobbyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RetrieveLobbyOptionsRequest that = (RetrieveLobbyOptionsRequest) o;
    return Objects.equals(lobbyId, that.lobbyId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lobbyId);
  }
}
