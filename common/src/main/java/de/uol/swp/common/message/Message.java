package de.uol.swp.common.message;

import de.uol.swp.common.user.Session;
import java.io.Serializable;
import java.util.Optional;

/**
 * Base interface of all messages.
 *
 * @see java.io.Serializable
 */
public interface Message extends Serializable {

  /**
   * Retrieve the current message context.
   *
   * @implNote .isPresent() to check if the MessageContext got set
   * @implNote .get() to get the MessageContext object
   * @return Empty optional object or MessageContext
   * @see de.uol.swp.common.message.MessageContext
   */
  Optional<MessageContext> getMessageContext();

  /**
   * Allows to set a MessageContext, e.g. for network purposes.
   *
   * @param messageContext the MessageContext to be set
   * @see de.uol.swp.common.message.MessageContext
   */
  void setMessageContext(MessageContext messageContext);

  /**
   * Retrieve current session.
   *
   * @implNote .isPresent() to check if the Session got set
   * @implNote .get() to get the Session object
   * @return Empty optional object or MessageContext
   */
  Optional<Session> getSession();

  /**
   * Set the current session.
   *
   * @param session the current session
   * @see de.uol.swp.common.user.Session
   */
  void setSession(Session session);

  /**
   * Allow to create a new message, based on the given one (copy).
   *
   * @param otherMessage original Message
   */
  void initWithMessage(Message otherMessage);
}
