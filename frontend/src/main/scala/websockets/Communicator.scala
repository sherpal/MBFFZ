package websockets

import java.nio.ByteBuffer

import com.raquo.airstream.core.Observer
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.ownership.Owner
import com.raquo.laminar.api.L.windowEvents
import gamemanager.Synchronization
import gamestate.actions.{GameAction, GameEnd}
import main.Main
import menus.Menus
import messages.Message
import messages.Message.{ActionList, Ping, Pong}
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket

import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8Array}

/**
  * The Communicator is in charge of WebSocket communication with the server.
  * It feeds different kind of streams when it receives messages.
  */
object Communicator extends Owner {

  private val password: String = Main.password

  /**
    * Reference to the WebSocket that talks to the server.
    */
  private val webSocket: WebSocket = new WebSocket(s"ws://${dom.window.location.host}/connect/$password")
  webSocket.binaryType = "arraybuffer"

  webSocket.onopen = (_: dom.Event) => {
    if (scala.scalajs.LinkingInfo.developmentMode) {
      println("WebSocket opened")
    }
    webSocket.send("Hello Server!") // why not

    if (scala.scalajs.LinkingInfo.developmentMode) {
      $wsStringMessage.addObserver(debugObserver)(owner = this)
    }

    synchronizer.computeLinkTime() // synchronizing watches with the server
  }

  /** Sends a [[Message]] to the server (is there a better way to do this!?) */
  def sendMessage(message: Message): Unit = {
    val array = Message.encodeToBytes(message)
    val arrayBuffer = new ArrayBuffer(array.length)
    val view = new Uint8Array(arrayBuffer)
    for (idx <- array.indices) {
      view(idx) = array(idx)
    }
    webSocket.send(arrayBuffer)
  }

  private val synchronizer: Synchronization = new Synchronization(sendMessage)

  /** Fed by the communication with the server, when it sends a GameAction */
  private val gameActionBus: EventBus[GameAction] = new EventBus[GameAction]()
  val $gameAction: EventStream[GameAction] = gameActionBus.events

  private def onMessage(message: Message): Unit = {
    message match {
      case _: GameEnd =>
        Menus.moveToPostGame()
      case gameAction: GameAction =>
        gameActionBus.writer.onNext(gameAction)
      case ActionList(actions) =>
        actions.foreach(action => gameActionBus.writer.onNext(action.asInstanceOf[GameAction]))
      case pong: Pong => synchronizer.receivePong(pong)
      case Ping(sendingTime) => sendMessage(Pong(sendingTime, new java.util.Date().getTime))
      case _ =>
        println(message)
    }
  }

  private def onMessage(byteBuffer: ByteBuffer): Unit = {
    onMessage(Message.decode(byteBuffer))
  }

  private def onMessage(arrayBuffer: ArrayBuffer): Unit = {
    onMessage(Message.decode({
      val uint8Array = new Uint8Array(arrayBuffer)
      uint8Array.toArray.map(_.asInstanceOf[Byte])
    }))
  }

  private def onMessage(blob: dom.Blob): Unit = {
    val fr = new dom.FileReader()
    fr.onload = (event: dom.UIEvent) => onMessage(event.target.asInstanceOf[js.Dynamic].result.asInstanceOf[ByteBuffer])
    fr.readAsArrayBuffer(blob)
  }

  /** Fed by the string messages received by the server. */
  private val stringMessageStream: EventBus[String] = new EventBus[String]()
  def $wsStringMessage: EventStream[String] = stringMessageStream.events

  private def onMessage(message: String): Unit = {
    stringMessageStream.writer.onNext(message) // this is beautiful
  }

  private val debugObserver = Observer(
    (message: String) =>
      println(s"I received: $message")
  )

  webSocket.onmessage = (event: dom.MessageEvent) => {
    event.data match { // dispatch with respect to the type of stuff received
      case msg: String => onMessage(msg)
      case arrayBuffer: ArrayBuffer => onMessage(arrayBuffer)
      case bytes: ByteBuffer => onMessage(bytes)
      case blob: dom.Blob => onMessage(blob)
      case msg =>
        println(msg)
    }
  }

  webSocket.onerror = (event: dom.Event) => {
    println("Error")
    println(event)
  }

  windowEvents.onBeforeUnload.map(_ => {
    // https://stackoverflow.com/questions/4812686/closing-websocket-correctly-html5-javascript
    webSocket.onclose = _ => {}
    webSocket.close()
  })

  @inline def getTime: Long = synchronizer.time

}