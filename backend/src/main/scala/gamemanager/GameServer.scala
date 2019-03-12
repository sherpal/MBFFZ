package gamemanager

import gamestate.actions.GameAction
import io.undertow.websockets.core.{BufferedBinaryMessage, BufferedTextMessage, WebSocketChannel}
import messages.Message
import messages.Message.{Ping, Pong}
import routes.websockets.{Client, Server}
import utils.Constants

import scala.collection.mutable

/**
  * Makes the communication with the client WebSockets.
  *
  */
final class GameServer extends Server[String] {

  /** Gets the client instance from its password. */
  private val _playersFromPW: mutable.Map[String, Client] = mutable.Map()

  /** Gets the client instance from its password. */
  def getClient(password: String): Option[Client] = _playersFromPW.get(password)

  def connectionCallback(client: Client, password: String): Unit = this.synchronized {
    /** Adding the client to the playersFromPW map, and tells everyone that a new player is connected. */
    _playersFromPW += password -> client
    broadcastText(Constants.playerListUpdate)

    if (PreGameManager.isHead(PreGameManager.playerName(password))) {
      sendTextToClient(Constants.youAreTheHead, client)
    }
  }

  /**
    * Sends a player the Id of its player in the game.
    * @param id       the id of the player
    * @param password the password of the corresponding player
    */
  def sendPlayerId(id: Long, password: String): Unit =
    sendTextToClient(s"id:${id.toString}", _playersFromPW(password))

  /** Sends a [[Message]] to the given channel. */
  def sendMessageToClient(message: Message, channel: WebSocketChannel): Unit =
    sendBytesToClient(Message.encode(message), channel)

  /** Sends a [[Message]] to every connected clients. */
  def broadcastMessage(message: Message): Unit =
    broadcastBytes(() => Message.encode(message))

  /** Basically does nothing. */
  override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
    message.getData match {
      case "" => channel.close()
      case _ =>
    }
  }

  /**
    * Manage entering binary messages.
    * If a binary message is received, this is because a [[Message]] instance was sent by the client,
    * so we decode it using the `decode` method.
    * @param channel origin of the message
    * @param raw raw binary message
    */
  override def onFullBinaryMessage(channel: WebSocketChannel, raw: BufferedBinaryMessage): Unit = this.synchronized {
    val arrayByteBuffer = raw.getData.getResource

    val message = Message.decode(arrayByteBuffer.head)

    message match {
      case Ping(sendingTime) =>
        sendMessageToClient(Pong(sendingTime, new java.util.Date().getTime), channel)
      case gameAction: GameAction =>
        Manager.gameManager match {
          case Some(gameManager) => gameManager.enqueue(gameAction)
          case _ =>
        }
      case _: Pong =>
        updateMessageReceived(clientFromChannel(channel))
      case _ =>
        println(message)
    }
  }

  def closeCallback(channel: WebSocketChannel, client: Client): Unit = {
    if (!Manager.playing) {
      _playersFromPW.find(_._2 == client).map(_._1).foreach(PreGameManager.removePlayer)
    } else {
      // todo
    }
  }

}
