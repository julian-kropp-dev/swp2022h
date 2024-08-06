package de.uol.swp.common.lobby.request;

import de.uol.swp.common.lobby.response.AllLobbyIdResponse;
import de.uol.swp.common.message.AbstractRequestMessage;

/**
 * Request for initialising the lobby list in the client.
 *
 * <p>This message is sent during the initialization of the lobby list. The server will respond with
 * a AllLobbyIdResponse. The AllLobbyIdResponse contains all Ids of all public lobbys.
 *
 * @see AllLobbyIdResponse
 */
public class AllLobbyIdRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 45124383619394L;
}
