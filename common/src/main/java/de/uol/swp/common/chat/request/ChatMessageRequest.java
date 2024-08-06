package de.uol.swp.common.chat.request;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.message.AbstractRequestMessage;
import java.util.Objects;

/** The message asks the server whether a new message can be pushed to the user. */
public class ChatMessageRequest extends AbstractRequestMessage {

  private static final long serialVersionUID = 5554322460035460471L;
  private final ChatMessageDTO message;

  public ChatMessageRequest(ChatMessageDTO message) {
    this.message = message;
  }

  /**
   * Methode for returning the message from the user.
   *
   * @return message from the user
   */
  public ChatMessageDTO getMessage() {
    return message;
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
    ChatMessageRequest that = (ChatMessageRequest) o;
    return Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), message);
  }
}
