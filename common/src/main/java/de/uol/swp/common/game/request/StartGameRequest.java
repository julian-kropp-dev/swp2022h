package de.uol.swp.common.game.request;

import de.uol.swp.common.lobby.request.AbstractLobbyRequest;
import de.uol.swp.common.user.UserDTO;
import java.util.Objects;

/** Request for starting the game. */
public class StartGameRequest extends AbstractLobbyRequest {

  private static final long serialVersionUID = 7964501674571676704L;
  private boolean withCommandStarted;

  public StartGameRequest(String name, UserDTO user) {
    super(name, user);
    withCommandStarted = false;
  }

  public StartGameRequest(String name, UserDTO user, boolean withCommand) {
    super(name, user);
    withCommandStarted = withCommand;
  }

  public boolean isWithCommandStarted() {
    return withCommandStarted;
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
    StartGameRequest that = (StartGameRequest) o;
    return withCommandStarted == that.withCommandStarted;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), withCommandStarted);
  }
}
