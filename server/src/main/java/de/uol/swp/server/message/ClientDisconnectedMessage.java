package de.uol.swp.server.message;

/**
 * This message is posted onto the EventBus if a client disconnects
 *
 * <p>This message is posted by the ServerHandler if a client disconnects. Within the BaseProject
 * this message is not processed after posting (Maybe it would be a good idea to use this message
 * for something)
 *
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.communication.ServerHandler#clientDisconnected
 * @see de.uol.swp.server.usermanagement.AuthenticationService
 */
public class ClientDisconnectedMessage extends AbstractServerInternalMessage {

  private static final long serialVersionUID = 6063061527545209307L;
}
