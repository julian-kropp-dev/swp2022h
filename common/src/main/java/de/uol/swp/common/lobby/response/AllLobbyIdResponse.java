package de.uol.swp.common.lobby.response;

import de.uol.swp.common.lobby.Lobby;
import de.uol.swp.common.lobby.dto.LobbyDTO;
import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Class for AllLobbyIdResponse. */
public class AllLobbyIdResponse extends AbstractResponseMessage {

  private static final long serialVersionUID = -5318208659057296729L;
  private final ArrayList<LobbyDTO> lobbys = new ArrayList<>();

  /**
   * Default Constructor.
   *
   * @implNote this constructor is needed for serialization
   */
  @SuppressWarnings("unused")
  public AllLobbyIdResponse() {
    // needed for serialization
  }

  /**
   * Constructor
   *
   * <p>This constructor generates a new List of all lobbies from the given Collection. The
   * significant difference between the two being that the new List contains copies of the Lobby
   * objects.
   *
   * @param lobbies Collection of all lobbies
   */
  public AllLobbyIdResponse(Collection<Lobby> lobbies) {
    for (Lobby lobby : lobbies) {
      this.lobbys.add((LobbyDTO) lobby);
    }
  }

  /**
   * Getter for the list lobbies.
   *
   * @return list of all lobbies
   */
  public List<LobbyDTO> getLobbies() {
    return lobbys;
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
    AllLobbyIdResponse that = (AllLobbyIdResponse) o;
    return Objects.equals(lobbys, that.lobbys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lobbys);
  }
}
