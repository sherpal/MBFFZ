package websockets

import java.nio.ByteBuffer

import gamemanager.{Game, Synchronization}
import gamestate.actions.{GameAction, GameEnd}
import menus.{MenuDisplay, Menus}
import messages.Message
import messages.Message.{ActionList, Ping, Pong}
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket

import scala.scalajs.js
import scala.scalajs.js.timers._
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8Array}

final class Communicator private (password: String) {

  val webSocket: WebSocket = new WebSocket(s"ws://${dom.window.location.hostname}:8080/connect/$password")
  webSocket.binaryType = "arraybuffer"

  webSocket.onopen = (_: dom.Event) => {
    println("WebSocket opened")
    webSocket.send("Hello Server!")

    synchronizer.computeLinkTime()
  }

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

  private def onMessage(message: Message): Unit = {
    message match {
      case pong: Pong => synchronizer.receivePong(pong)
      case _: GameEnd =>
        Menus.moveToPostGame()
      case gameAction: GameAction =>
        Game.game.receiveAction(gameAction)
      case ActionList(actions) =>
        Game.game.receiveActions(actions.map(_.asInstanceOf[GameAction]))
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

  private def onMessage(message: String): Unit = message match {
    case "player list update" =>
      MenuDisplay.changePlayerList()
    case "you're the head" =>
      MenuDisplay.setLaunchButton()
    case idInfo if idInfo.startsWith("id:") =>
      Game(idInfo.substring(3))
    case _ =>
      println(s"Don't know how to process: $message")
  }

  webSocket.onmessage = (event: dom.MessageEvent) => {

    val message = event.data
    message match {
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

  @inline def getTime: Long = synchronizer.time

}

object Communicator {

  private var _communicator: Communicator = _
  @inline def communicator: Communicator = _communicator

  def apply(password: String): Communicator = {
    _communicator = new Communicator(password)
    communicator
  }

}