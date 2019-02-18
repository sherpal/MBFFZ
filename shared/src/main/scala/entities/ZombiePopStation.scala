package entities

import physics.Complex
import physics.shape.Circle

final class ZombiePopStation(val id: Long, val time: Long, val pos: Complex) extends Body {

  val rotation: Double = 0

  val shape: Circle = ZombiePopStation.shape

}

object ZombiePopStation {

  val popStationRadius: Double = 5

  val shape: Circle = new Circle(5)

}