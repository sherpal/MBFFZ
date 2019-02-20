package routes.websockets

import java.nio.ByteBuffer

import io.undertow.websockets.core._
import messages.Message
import messages.Message.Ping

import scala.collection.mutable

trait Server[T] extends AbstractReceiveListener {

  private val webSocketCallback: WebSocketCallback[Void] = new WebSocketCallback[Void] {
    override def complete(channel: WebSocketChannel, context: Void): Unit = {}

    override def onError(channel: WebSocketChannel, context: Void, throwable: Throwable): Unit = {}
  }

  private val clients: mutable.Map[WebSocketChannel, Client] = mutable.Map()

  @inline protected def clientFromChannel(channel: WebSocketChannel): Client = clients(channel)

  private val lastMessageReceived: mutable.Map[Client, Long] = mutable.Map()

  def sendTextToClient(text: String, client: Client): Unit = {
    sendTextToClient(text, client.channel)
  }

  def sendTextToClient(text: String, channel: WebSocketChannel): Unit = {
    WebSockets.sendText(text, channel, webSocketCallback)
  }

  def broadcastText(text: String): Unit =
    clients.values.foreach(sendTextToClient(text, _))

  def sendBytesToClient(buffer: ByteBuffer, client: Client): Unit =
    sendBytesToClient(buffer, client.channel)

  def sendBytesToClient(bytes: ByteBuffer, channel: WebSocketChannel): Unit = {
    WebSockets.sendBinary(bytes, channel, webSocketCallback)
  }

  def broadcastBytes(bytes: () => ByteBuffer): Unit =
    clients.values.foreach(sendBytesToClient(bytes(), _))

  def connectionCallback(client: Client, arg: T): Unit

  def clientConnected(client: Client, arg: T): Unit = {
    clients += (client.channel -> client)
    lastMessageReceived += (client -> new java.util.Date().getTime)
    client.channel.getReceiveSetter.set(this)
    client.channel.resumeReceives()

    connectionCallback(client, arg)

    println(s"New client from ${client.address}")

    broadcastText(s"New client from ${client.address}")
  }

  def closeCallback(channel: WebSocketChannel, client: Client): Unit

  override def onClose(webSocketChannel: WebSocketChannel, channel: StreamSourceFrameChannel): Unit = {
    closingClient(webSocketChannel)
  }

  private def closingClient(channel: WebSocketChannel): Unit = {
    println("closed")
    val client = clients(channel)
    clients -= channel
    lastMessageReceived -= client
    closeCallback(channel, client)
  }

  def broadcastPing(): Unit = {
    val time = new java.util.Date().getTime
    val message = Ping(time)
    broadcastBytes(() => Message.encode(message))
  }

  protected def updateMessageReceived(client: Client): Unit =
    lastMessageReceived += client -> new java.util.Date().getTime


  private lazy val checkConnectionsThread: Thread = new Thread {

    override def run(): Unit = {
      broadcastPing()
      Thread.sleep(2000)
      broadcastPing()
      Thread.sleep(2000)

      val time = new java.util.Date().getTime
      for {
        (channel, client) <- clients
        if time - lastMessageReceived(client) > 5000
      } {
        println("killing someone")
        closingClient(channel)
        channel.close()
      }
      run()
    }
  }

  def startCheckConnection(): Unit =
    checkConnectionsThread.start()

  startCheckConnection()

  def killAll(): Unit = {
    for ((channel, _) <- clients) channel.close()
  }

}
