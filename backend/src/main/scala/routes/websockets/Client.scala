package routes.websockets

import io.undertow.websockets.core.WebSocketChannel

/**
  * Client is an abstraction on top of WebSocketChannel.
  * Not sure it's relevant at this stage...
  */
final class Client(
                     val channel: WebSocketChannel
                  ) {

  def address: String = channel.getPeerAddress.toString

}
