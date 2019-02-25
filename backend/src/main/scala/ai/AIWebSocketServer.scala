package ai

import gamestate.GameState
import io.undertow.websockets.core.{BufferedTextMessage, WebSocketChannel}
import routes.websockets.{Client, Server}
import outsidemessages.GameStateCom.FromGameState
import outsidemessages.{Direction, LaunchGame}
import upickle.default._

import scala.collection.mutable

final class AIWebSocketServer(val gameName: String) extends Server[String] {

  def closeCallback(channel: WebSocketChannel, client: Client): Unit = {
    println("channel close")
  }

  private val clientsByName: mutable.Map[String, Client] = mutable.Map()

  def sendToAI(message: String, name: String): Unit =
    sendTextToClient(message, clientsByName(name))

  def sendMessageWithType(messageType: String, message: String, name: String): Unit =
    sendToAI(s"""{"$messageType": $message}""", name)

  def broadcastMessageWithType(messageType: String, message: String): Unit =
    broadcastText(s"""{"$messageType": $message}""")

  def broadcastGameState(gameState: GameState): Unit =
    broadcastMessageWithType("GameState", gameState.toGSComString)

  def connectionCallback(client: Client, aiName: String): Unit = {
    clientsByName += aiName -> client
    println(s"$aiName joined the game")
  }

  override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
    val data = message.getData
    val messageTypeDelimiter = data.indexOf(":")
    val messageType = data.substring(0, messageTypeDelimiter)
    val messageContent = data.substring(messageTypeDelimiter + 1)

    (messageType, messageContent) match {
      case ("Direction", directionStr) =>
        val direction = read[Direction](directionStr)
        AIManager.gameTrainer(gameName).receiveDirections(direction)
      case ("LaunchGame", launchGameStr) =>
        val launchGame = read[LaunchGame](launchGameStr)
        AIManager.launchGame(launchGame.gameName, launchGame.timeStamp, clientsByName.keys.toList)
      case m =>
        println(m)
    }
  }


}
