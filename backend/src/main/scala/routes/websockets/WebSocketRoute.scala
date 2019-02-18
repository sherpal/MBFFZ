package routes.websockets

import gamemanager.{Manager, PreGameManager}
import io.undertow.websockets.WebSocketConnectionCallback
import io.undertow.websockets.core.WebSocketChannel
import io.undertow.websockets.spi.WebSocketHttpExchange

object WebSocketRoute extends cask.Routes {


  @cask.websocket("/connect/:password")
  def websocketConnect(password: String): cask.WebsocketResult = {
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

  initialize()

}
