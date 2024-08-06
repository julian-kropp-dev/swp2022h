package de.uol.swp.common.message;

/**
 * Base class of all request messages. Basic handling of messages from the client to the server
 *
 * @see de.uol.swp.common.message.AbstractMessage
 * @see de.uol.swp.common.message.RequestMessage
 */
public abstract class AbstractRequestMessage extends AbstractMessage implements RequestMessage {

  private static final long serialVersionUID = 5366167153636367089L;

  @Override
  public boolean authorizationNeeded() {
    return true;
  }
}
