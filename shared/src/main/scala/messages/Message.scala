package messages

import java.nio.ByteBuffer

import gamestate.actions._
import boopickle.Default._
import boopickle.CompositePickler
import physics.Complex

trait Message

/**
  * boopickle stuff
  */
object Message {

  case class Ping(sendingTime: Long) extends Message
  case class Pong(sendingTime: Long, midwayTime: Long) extends Message

  case class ActionList(gameActions: List[Message]) extends Message

  implicit private val messagePickler: CompositePickler[Message] = compositePickler[Message]
    .addConcreteType[Complex]
    .addConcreteType[GameEnd]
    .addConcreteType[InitialGameState]
    .addConcreteType[NewPlayerInfo]
    .addConcreteType[NewPopStations]
    .addConcreteType[PlayerDies]
    .addConcreteType[PopStationsPop]
    .addConcreteType[UpdatePlayerPos]
    .addConcreteType[UpdateZombies]
    .addConcreteType[ZombieUpdateInfo]
    .addConcreteType[ActionList]
    .addConcreteType[Ping]
    .addConcreteType[Pong]

  def decode(buffer: Array[Byte]): Message =
    Unpickle[Message](messagePickler).fromBytes(ByteBuffer.wrap(buffer))

  def decode(byteBuffer: ByteBuffer): Message =
    Unpickle[Message](messagePickler).fromBytes(byteBuffer)

  def encodeToBytes(message: Message): Array[Byte] = {
    val byteBuffer = Pickle.intoBytes(message)
    val array = new Array[Byte](byteBuffer.remaining())
    byteBuffer.get(array)
    array
  }

  def encode(message: Message): ByteBuffer = Pickle.intoBytes(message)

}