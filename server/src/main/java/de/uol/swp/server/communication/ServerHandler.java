package de.uol.swp.server.communication;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.uol.swp.common.message.ExceptionMessage;
import de.uol.swp.common.message.Message;
import de.uol.swp.common.message.MessageContext;
import de.uol.swp.common.message.RequestMessage;
import de.uol.swp.common.message.ResponseMessage;
import de.uol.swp.common.message.ServerMessage;
import de.uol.swp.common.user.Session;
import de.uol.swp.common.user.message.UserLoggedInMessage;
import de.uol.swp.common.user.message.UserLoggedOutMessage;
import de.uol.swp.common.user.response.LoginSuccessfulResponse;
import de.uol.swp.common.user.response.LogoutSuccessfulResponse;
import de.uol.swp.server.message.ClientAuthorizedMessage;
import de.uol.swp.server.message.ClientDisconnectedMessage;
import de.uol.swp.server.message.ClientLoggedOutMessage;
import de.uol.swp.server.message.ServerExceptionMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.security.auth.login.LoginException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles all client/server communication.
 *
 * @see de.uol.swp.server.communication.ServerHandlerDelegate
 */
@SuppressWarnings("UnstableApiUsage")
public class ServerHandler implements ServerHandlerDelegate {

  private static final Logger LOG = LogManager.getLogger(ServerHandler.class);
  /** Clients with logged-in sessions. */
  protected final Map<MessageContext, Session> activeSessions = new HashMap<>();
  /** Clients that are connected. */
  private final List<MessageContext> connectedClients = new CopyOnWriteArrayList<>();
  /** Event bus (injected). */
  private final EventBus eventBus;

  /**
   * Constructor.
   *
   * @param eventBus the EventBus used throughout the entire server
   * @see EventBus
   */
  @Inject
  public ServerHandler(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(this);
  }

  @Override
  public void process(RequestMessage msg) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Received new message from client {}", msg);
    }
    final Optional<MessageContext> messageContext = msg.getMessageContext();
    if (messageContext.isPresent()) {
      try {
        checkIfMessageNeedsAuthorization(messageContext.get(), msg);
        eventBus.post(msg);
      } catch (Exception e) {
        LOG.error("ServerException {} {}", e.getClass().getName(), e.getMessage());
        sendToClient(messageContext.get(), new ExceptionMessage(e.getMessage()));
      }
    } else {
      if (LOG.isErrorEnabled()) {
        LOG.error(String.format("No message context for %s!", msg));
      }
    }
  }

  /**
   * Helper method that check if a Message has the required authorization.
   *
   * @param ctx the MessageContext connected to the message to check
   * @param msg the message to check
   * @throws SecurityException authorization requirement not met
   */
  private void checkIfMessageNeedsAuthorization(MessageContext ctx, RequestMessage msg) {
    if (msg.authorizationNeeded()) {
      final Optional<Session> session = getSession(ctx);
      if (session.isEmpty()) {
        LOG.warn("The client is not authorized for message {}", msg);
        throw new SecurityException("Autorisation benötigt! \nClient ist nicht eingeloggt.");
      }
      msg.setSession(session.get());
    }
  }

  /**
   * Handles exceptions on the Server.
   *
   * <p>If an ServerExceptionMessage is detected on the EventBus, this method is called. It sends
   * the ServerExceptionMessage to the affiliated client if a client is affiliated.
   *
   * @param msg The ServerExceptionMessage found on the EventBus
   */
  @Subscribe
  protected void onServerExceptionMessage(ServerExceptionMessage msg) {
    Optional<MessageContext> ctx = getCtx(msg);
    if (msg.getException() instanceof LoginException) {
      LOG.warn(msg.getException());
    } else {
      LOG.error(msg.getException());
    }
    ctx.ifPresent(
        channelHandlerContext ->
            sendToClient(
                channelHandlerContext, new ExceptionMessage(msg.getException().getMessage())));
  }

  /**
   * Handles errors produced by the EventBus
   *
   * <p>If an DeadEvent object is detected on the EventBus, this method is called. It writes
   * "DeadEvent detected " and the error message of the detected DeadEvent object to the log, if the
   * loglevel is set to WARN or higher.
   *
   * @param deadEvent The DeadEvent object found on the EventBus
   */
  @Subscribe
  private void onDeadEvent(DeadEvent deadEvent) {
    LOG.error("DeadEvent detected {}", deadEvent);
  }

  // -------------------------------------------------------------------------------
  // Handling of connected clients
  // -------------------------------------------------------------------------------
  @Override
  public void newClientConnected(MessageContext ctx) {
    LOG.info("New client {} connected", ctx);
    connectedClients.add(ctx);
  }

  @Override
  public void clientDisconnected(MessageContext ctx) {
    LOG.debug("Client disconnected");
    Session session = this.activeSessions.get(ctx);
    if (session != null) {
      ClientDisconnectedMessage msg = new ClientDisconnectedMessage();
      msg.setSession(session);
      eventBus.post(msg);
      removeSession(ctx);
    }
    connectedClients.remove(ctx);
  }

  // -------------------------------------------------------------------------------
  // User Management Events (from event bus)
  // -------------------------------------------------------------------------------
  /**
   * Handles ClientAuthorizedMessages found on the EventBus
   *
   * <p>If a ClientAuthorizedMessage is detected on the EventBus, this method is called. It gets the
   * MessageContext and then gives it and a new LoginSuccessfulResponse to sendToClient for sending
   * as well as giving a new UserLoggedInMessage to sendMessage for notifying all connected clients.
   *
   * @param msg The ClientAuthorizedMessage found on the EventBus
   * @see de.uol.swp.server.communication.ServerHandler#sendToClient(MessageContext,
   *     ResponseMessage)
   * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
   */
  @Subscribe
  protected void onClientAuthorizedMessage(ClientAuthorizedMessage msg) {
    Optional<MessageContext> ctx = getCtx(msg);
    final Optional<Session> session = msg.getSession();
    if (ctx.isPresent() && session.isPresent()) {
      putSession(ctx.get(), session.get());
      sendToClient(ctx.get(), new LoginSuccessfulResponse(msg.getUser()));
      sendMessage(new UserLoggedInMessage(msg.getUser().getUsername()));
    } else {
      LOG.warn("No context for {}", msg);
    }
  }

  /**
   * Handles ClientLoggedOutMessages found on the EventBus.
   *
   * <p>If an ClientLoggedOutMessage is detected on the EventBus, this method is called. It gets the
   * MessageContext and then gives the message to sendMessage in order to send it to the connected
   * client.
   *
   * @param msg The ClientLoggedOutMessage found on the EventBus
   * @see de.uol.swp.server.communication.ServerHandler#sendToClient(MessageContext,
   *     ResponseMessage)
   * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
   */
  @Subscribe
  protected void onUserLoggedOutMessage(ClientLoggedOutMessage msg) {
    Optional<MessageContext> ctx = getCtx(msg);
    final Optional<Session> session = msg.getSession();
    if (ctx.isPresent() && session.isPresent()) {
      removeSession(ctx.get());
      sendToClient(ctx.get(), new LogoutSuccessfulResponse(msg.getUser()));
      sendMessage(new UserLoggedOutMessage(msg.getUser().getUsername()));
    } else {
      LOG.warn("No context for {}", msg);
    }
  }

  // -------------------------------------------------------------------------------
  // ResponseEvents
  // -------------------------------------------------------------------------------

  /**
   * Handles ResponseMessages found on the EventBus
   *
   * <p>If an ResponseMessage is detected on the EventBus, this method is called. It gets the
   * MessageContext and then gives it and the ResponseMessage to sendToClient for sending.
   *
   * @param msg The ResponseMessage found on the EventBus
   * @see de.uol.swp.server.communication.ServerHandler#sendToClient(MessageContext,
   *     ResponseMessage)
   */
  @Subscribe
  private void onResponseMessage(ResponseMessage msg) {
    Optional<MessageContext> ctx = getCtx(msg);
    if (ctx.isPresent()) {
      msg.setSession(null);
      msg.setMessageContext(null);
      LOG.debug("Send to client {} message {} ", ctx.get(), msg);
      sendToClient(ctx.get(), msg);
    } else {
      LOG.warn("Got response message without receiver {}", msg);
    }
  }

  // -------------------------------------------------------------------------------
  // ServerMessages
  // -------------------------------------------------------------------------------

  /**
   * Handles ServerMessages found on the EventBus
   *
   * <p>If an ServerMessage is detected on the EventBus, this method is called. It sets the Session
   * and MessageContext to null and then gives the message to sendMessage in order to send it to all
   * connected clients.
   *
   * @param msg The ServerMessage found on the EventBus
   * @see de.uol.swp.server.communication.ServerHandler#sendMessage(ServerMessage)
   */
  @Subscribe
  private void onServerMessage(ServerMessage msg) {
    msg.setSession(null);
    msg.setMessageContext(null);
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "Send {} to {}",
          msg,
          (msg.getReceiver().isEmpty() || msg.getReceiver() == null ? "all" : msg.getReceiver()));
    }
    sendMessage(msg);
  }

  // -------------------------------------------------------------------------------
  // Session Management (helper methods)
  // -------------------------------------------------------------------------------

  /**
   * Adds a new Session to the activeSessions.
   *
   * @param ctx The MessageContext belonging to the Session
   * @param newSession the Session to add
   */
  private void putSession(MessageContext ctx, Session newSession) {
    activeSessions.put(ctx, newSession);
  }

  /**
   * Removes a Session specified by MessageContext from the activeSessions.
   *
   * @param ctx the MessageContext
   */
  protected void removeSession(MessageContext ctx) {
    activeSessions.remove(ctx);
  }

  /**
   * Gets the Session for a given MessageContext.
   *
   * @param ctx The MeesageContext
   * @see de.uol.swp.common.user.Session
   * @see de.uol.swp.common.message.MessageContext
   * @return Optional containing the Session if found
   */
  private Optional<Session> getSession(MessageContext ctx) {
    Session session = activeSessions.get(ctx);
    return session != null ? Optional.of(session) : Optional.empty();
  }

  /**
   * Gets MessageContext from Message.
   *
   * @param message Message to get the MessageContext from
   * @see de.uol.swp.common.message.Message
   * @see de.uol.swp.common.message.MessageContext
   * @return Optional containing the MessageContext if there is any
   */
  protected Optional<MessageContext> getCtx(Message message) {
    if (message.getMessageContext().isPresent()) {
      return message.getMessageContext();
    }
    final Optional<Session> session = message.getSession();
    if (session.isPresent()) {
      return getCtx(session.get());
    }
    return Optional.empty();
  }

  /**
   * Gets MessageContext for specified receiver.
   *
   * @param session Session of the user to search
   * @see de.uol.swp.common.user.Session
   * @see de.uol.swp.common.message.MessageContext
   * @return Optional containing MessageContext if there is one
   */
  private Optional<MessageContext> getCtx(Session session) {
    for (Map.Entry<MessageContext, Session> e : activeSessions.entrySet()) {
      if (e.getValue().equals(session)) {
        return Optional.of(e.getKey());
      }
    }
    return Optional.empty();
  }

  /**
   * Gets MessageContexts for specified receivers.
   *
   * @param receiver A list containing the sessions of the users to search
   * @see de.uol.swp.common.user.Session
   * @see de.uol.swp.common.message.MessageContext
   * @return List of MessageContexts for the given sessions
   */
  private List<MessageContext> getCtx(List<Session> receiver) {
    List<MessageContext> ctxs = new ArrayList<>();
    receiver.forEach(
        r -> {
          Optional<MessageContext> s = getCtx(r);
          s.ifPresent(ctxs::add);
        });
    return ctxs;
  }

  // -------------------------------------------------------------------------------
  // Help methods: Send only objects of type Message
  // -------------------------------------------------------------------------------

  /**
   * Sends a ResponseMessage to a client specified by a MessageContext.
   *
   * @param ctx The MessageContext containing the specified client
   * @param message The Message to send
   * @see de.uol.swp.common.message.ResponseMessage
   * @see de.uol.swp.common.message.MessageContext
   */
  protected void sendToClient(MessageContext ctx, ResponseMessage message) {
    LOG.trace("Trying to sendMessage to client: {} {}", ctx, message);
    ctx.writeAndFlush(message);
  }

  /**
   * Sends a ServerMessage to either a specified receiver or all connected clients.
   *
   * @param msg ServerMessage to send
   * @see de.uol.swp.common.message.ServerMessage
   */
  protected void sendMessage(ServerMessage msg) {
    if (msg.getReceiver() == null || msg.getReceiver().isEmpty()) {
      sendToMany(connectedClients, msg);
    } else {
      sendToMany(getCtx(msg.getReceiver()), msg);
    }
  }

  /**
   * Sends a ServerMessage to multiple users specified by a list of MessageContexts.
   *
   * @param sendTo List of MessageContexts to send the message to
   * @param msg message to send
   * @see de.uol.swp.common.message.MessageContext
   * @see de.uol.swp.common.message.ServerMessage
   */
  private void sendToMany(List<MessageContext> sendTo, ServerMessage msg) {
    for (MessageContext client : sendTo) {
      try {
        client.writeAndFlush(msg);
      } catch (Exception e) {
        LOG.warn(e);
      }
    }
  }
}
