package entities

import gamestate.GameState
import physics.Complex
import physics.quadtree.ShapeQT
import entities.ZombiePopStation.shape
import gamestate.actions._

import scala.util.Random

final class PopStationContainer(
                                 val time: Long,
                                 val popStations: Map[Long, ZombiePopStation],
                                 val lastPop: Long,
                               ) {

  def updateZombiePopStations(
                               currentTime: Long,
                               numberToPop: Int,
                               popTime: Long,
                               quadTree: ShapeQT
                             ): (Option[NewPopStations], Option[PopStationsPop]) = {

    def findPos(): Complex = {
      val tryPos = Complex(
        Random.nextDouble() * GameState.worldBox.width - GameState.worldBox.width / 2,
        Random.nextDouble() * GameState.worldBox.height - GameState.worldBox.height / 2
      )

      if (quadTree.collides(shape, tryPos, 0)) findPos()
      else tryPos
    }

    val newPopStations = if (currentTime - lastPop > popTime)
      (0 until numberToPop).toList.map(_ => new ZombiePopStation(
        Entity.newId(), currentTime, findPos()
      ))
    else List()

    val (popped, still) = popStations.values.partition(currentTime - _.time > PopStationContainer.gestation)

    val zombies = popped.map(station => new Zombie(
      Entity.newId(), currentTime, station.pos, 0, moving = false
    ))

    (
      new PopStationContainer(
        currentTime,
        (still ++ newPopStations).map(popStation => popStation.id -> popStation).toMap,
        if (newPopStations.nonEmpty) currentTime else lastPop
      ),
      zombies.map(zombie => zombie.id -> zombie).toMap
    )


    (
      if (newPopStations.isEmpty) None
      else Some(NewPopStations(
        GameAction.id(), currentTime,
        newPopStations.map(popStation => (popStation.id, popStation.pos))
      )),
      if (zombies.isEmpty) None
      else Some(PopStationsPop(
        GameAction.id(), currentTime,
        popped.map(_.id).toList,
        zombies.map(_.id).toList
      ))
    )

  }


}

object PopStationContainer {

  final val gestation: Long = 2000

  final val popTime: Long = 10000

}