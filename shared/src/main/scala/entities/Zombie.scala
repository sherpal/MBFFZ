package entities

import gamestate.actions.{GameAction, PlayerDies, UpdateZombies, ZombieUpdateInfo}
import physics.Complex
import physics.pathfinding.immutablegraph.{AntoineGraph, Graph}
import physics.quadtree.ShapeQT
import physics.shape.{Polygon, Segment, Shape}

final class Zombie(
                    val id: Long,
                    val time: Long,
                    val pos: Complex,
                    val direction: Double,
                    val moving: Boolean
                  ) extends MovingBody {

  val speed: Double = Zombie.zombieSpeed
  val rotation: Double = direction

  val shape: Polygon = Zombie.zombieShape

}

object Zombie {

  val zombieSpeed: Double = 75

  val zombieRadius: Double = 10

  val zombieShape: Polygon = Shape.regularPolygon(3, zombieRadius)

  def updateZombies(
                     time: Long,
                     zombies: Map[Long, Zombie],
                     players: Set[Player],
                     graph: Graph,
                     quadTree: ShapeQT,
                     inflatedEdges: List[Segment]
                   ): UpdateZombies = {

    val playerPositions = players.map(_.pos)

    val newGraph = playerPositions.foldLeft(graph)({
      case (currentGraph, pos) => new Graph(
        pos +: currentGraph.vertices, AntoineGraph.addVertex(
          pos, currentGraph.neighboursMap, quadTree, inflatedEdges, forceInclusion = true
        )
      )
    })

    val remainingAndDeletedZombies = zombies.values
      .groupBy(zombie => (zombie.pos.re.toInt / 3, zombie.pos.im.toInt / 3))
      .values
      .map(zombiesThere => (zombiesThere.head, zombiesThere.tail))

    val remainingZombies = remainingAndDeletedZombies.map(_._1)
    val deleteZombies = remainingAndDeletedZombies.flatMap(_._2.map(_.id))

//    val newZombies = zombies.values.groupBy(zombie => zombie.currentPosition(time - zombie.time).toIntComplex)
//      .map {
//        case (currentPosition, zombiesThere) =>
//          val target = playerPositions.minBy(pos => (pos - currentPosition).modulus2)
//
//          val zombie = zombiesThere.head // fusion all zombies at same position
//          val deletedZombies = zombiesThere.tail
//
//          (
//            new Graph(
//              currentPosition +: newGraph.vertices,
//              AntoineGraph.addVertex(
//                currentPosition, newGraph.neighboursMap, quadTree, inflatedEdges,
//                forceInclusion = true
//              )
//            ).a_*(currentPosition, target, (z1, z2) => !(z1 - z2)) match {
//              case Some(path) if path.tail.nonEmpty =>
//                new Zombie(
//                  zombie.id, time,
//                  zombie.currentPosition(time - zombie.time),
//                  (path.tail.head - path.head).arg,
//                  moving = true
//                )
//              case _ =>
//                new Zombie(zombie.id, time, currentPosition, zombie.direction, moving = false)
//            },
//            deletedZombies
//          )
//      }

    val newZombies = remainingZombies.map({ zombie =>
      val currentPosition = zombie.currentPosition(time - zombie.time)
      val target = playerPositions.minBy(pos => (pos - currentPosition).modulus2)

      new Graph(
        currentPosition +: newGraph.vertices,
        AntoineGraph.addVertex(
          currentPosition, newGraph.neighboursMap, quadTree, inflatedEdges,
          forceInclusion = true
        )
      ).a_*(currentPosition, target, (z1, z2) => !(z1 - z2)) match {
        case Some(path) if path.tail.nonEmpty =>
          new Zombie(zombie.id, time, currentPosition, (path.tail.head - path.head).arg, moving = true)
        case _ =>
          new Zombie(zombie.id, time, currentPosition, zombie.direction, moving = false)
      }
    })

    UpdateZombies(
      GameAction.id(),
      time,
      newZombies.map(zombie => ZombieUpdateInfo(
        zombie.id, zombie.pos, zombie.direction, zombie.moving
      )).toList,
      deleteZombies.toList
    )
  }

  def findDeadPlayers(players: Traversable[Player], zombies: Traversable[Zombie], time: Long): List[PlayerDies] = {
    zombies.groupBy(zombie => players.minBy(player => (player.pos - zombie.pos).modulus2))
      .filter {
        case (player, closeZombies) =>
          closeZombies.exists(_.collides(player, time))
      }
      .map(playerAndZombies => PlayerDies(GameAction.id(), time, playerAndZombies._1.id))
      .toList
  }

}