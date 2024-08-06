package de.uol.swp.common.chat.message;

import de.uol.swp.common.chat.ChatMessageDTO;
import de.uol.swp.common.message.AbstractServerMessage;
import java.util.Objects;

/** Class for ChatMessages. */
public class ChatMessageMessage extends AbstractServerMessage {
  private static final long serialVersionUID = 7793454958390539481L;

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final ChatMessageDTO chatMessageDTO;

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public ChatMessageMessage(ChatMessageDTO chatMessageDTO) {
    this.chatMessageDTO = chatMessageDTO;
  }

  public ChatMessageDTO getChatMessage() {
    return chatMessageDTO;
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
    ChatMessageMessage that = (ChatMessageMessage) o;
    return Objects.equals(chatMessageDTO, that.chatMessageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chatMessageDTO);
  }
}
