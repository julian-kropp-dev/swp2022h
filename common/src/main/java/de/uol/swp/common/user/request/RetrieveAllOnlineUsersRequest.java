package de.uol.swp.common.user.request;

import de.uol.swp.common.message.AbstractRequestMessage;

/**
 * Request for initialising the user list in the client
 *
 * <p>This message is sent during the initialization of the user list. The server will respond with
 * a AllOnlineUsersResponse.
 *
 * @see de.uol.swp.common.user.response.AllOnlineUsersResponse
 */
public class RetrieveAllOnlineUsersRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 4171384570761177911L;
}
