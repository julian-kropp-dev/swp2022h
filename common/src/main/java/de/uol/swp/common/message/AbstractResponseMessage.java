package de.uol.swp.common.message;

/**
 * Base class of all response messages. Basic handling of answers from the server to the client
 *
 * @see de.uol.swp.common.message.AbstractMessage
 * @see de.uol.swp.common.message.ResponseMessage
 */
public abstract class AbstractResponseMessage extends AbstractMessage implements ResponseMessage {

  private static final long serialVersionUID = 1094221816672067429L;
}
