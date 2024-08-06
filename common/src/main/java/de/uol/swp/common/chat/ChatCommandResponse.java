package de.uol.swp.common.chat;

import de.uol.swp.common.message.AbstractResponseMessage;
import java.util.Objects;

/** This class is like ChatMessage only for command responses. */
public class ChatCommandResponse extends AbstractResponseMessage {
  private static final long serialVersionUID = 7793454958390639481L;

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private final ChatMessageDTO chatMessageDTO;

  /**
   * Constructor of the class.
   *
   * @param chatMessageDTO The message to the client.
   */
  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  public ChatCommandResponse(ChatMessageDTO chatMessageDTO) {
    this.chatMessageDTO = chatMessageDTO;
  }

  /**
   * Getter of the ChatMessage.
   *
   * @return ChatMessage.
   */
  public ChatMessageDTO getChatMassage() {
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
    ChatCommandResponse that = (ChatCommandResponse) o;
    return Objects.equals(chatMessageDTO, that.chatMessageDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), chatMessageDTO);
  }
}
