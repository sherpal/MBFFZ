package routes.websockets

import ai.AIManager
import gamemanager.{Manager, PreGameManager}
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.spi.WebSocketHttpExchange
import outsidemessages.NewGame

object WebSocketRoute extends cask.Routes {


  /**
    * When a handshake is started by a Client, we check that the password exists. If it doesn't, we
    * abort the connection. If it does, we connect the [[Client]] to the [[Server]].
    * @param password game password given to the player when their resquested joining the game.
    */
  @cask.websocket("/connect/:password")
  def webSocketConnect(password: String): cask.WebsocketResult = {
    if (!PreGameManager.isPlaying(password)) {
      cask.Response(s"Incorrect password: $password", 400)
    } else {
      new WebSocketConnectionCallback() {
        def onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel): Unit = {
          //        channel.getReceiveSetter.set(
          //          new AbstractReceiveListener() {
          //            override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
          //              message.getData match{
          //                case "" => channel.close()
          //                case data => WebSockets.sendTextBlocking(userName + " " + data, channel)
          //              }
          //            }
          //          }
          //        )
          //
          //        channel.resumeReceives()
          Manager.server.clientConnected(new Client(channel), password)
        }
      }
    }
  }

  @cask.websocket("/ai/:gameName/:aiName")
  def aiWebSocketConnect(gameName: String, aiName: String): cask.WebsocketResult = {
    new WebSocketConnectionCallback() {
      override def onConnect(exchange: WebSocketHttpExchange, channel: WebSocketChannel): Unit = {
        if (AIManager.gameHasStarted(gameName)) {
          channel.setCloseReason("Game has already started.")
          channel.close()
        } else {
          AIManager.clientConnect(NewGame(gameName, aiName), new Client(channel))
        }
      }
    }
  }

  initialize()

}
