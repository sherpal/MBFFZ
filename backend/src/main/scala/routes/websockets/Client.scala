package routes.websockets

import io.undertow.websockets.core.WebSocketChannel

final class Client(
                     val channel: WebSocketChannel
                  ) {

  def address: String = channel.getPeerAddress.toString

}
