package de.uol.swp.server.message;

import de.uol.swp.common.message.AbstractMessage;

/**
 * This class is used to unify the different kinds of internal ServerMessages.
 *
 * @see de.uol.swp.common.message.AbstractMessage
 */
abstract class AbstractServerInternalMessage extends AbstractMessage
    implements ServerInternalMessage {

  private static final long serialVersionUID = 3830567026465650467L;
}
