package gamemanager

import gamestate.actions.GameAction
import io.undertow.websockets.core.{BufferedBinaryMessage, BufferedTextMessage, WebSocketChannel}
import messages.Message
import messages.Message.{Ping, Pong}
import routes.websockets.{Client, Server}
import utils.Constants

import scala.collection.mutable

final class GameServer extends Server[String] {

  private val _playersFromPW: mutable.Map[String, Client] = mutable.Map()

  def getClient(password: String): Option[Client] = _playersFromPW.get(password)

  def connectionCallback(client: Client, password: String): Unit = this.synchronized {
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

  def sendMessageToClient(message: Message, channel: WebSocketChannel): Unit =
    sendBytesToClient(Message.encode(message), channel)

  def broadcastMessage(message: Message): Unit =
    broadcastBytes(() => Message.encode(message))

  override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
    message.getData match {
      case "" => channel.close()
      case _ =>
    }
  }

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
