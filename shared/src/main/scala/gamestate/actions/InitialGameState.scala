package gamestate.actions

import entities.{Obstacle, Player, Zombie}
import gamestate.GameState
import physics.Complex
import physics.pathfinding.immutablegraph.{AntoineGraph, Graph}
import physics.quadtree.ShapeQT.TraversableToQuadTree

final case class InitialGameState(
                                   id: Long, time: Long,
                                   playersInfo: List[NewPlayerInfo],
                                   zombiesInfo: List[UpdatePlayerPos],
                                   obstaclePositions: List[Complex],
                                   computeGraph: Boolean = false
                                 ) extends GameAction {

  def apply(gameState: GameState): GameState = {
    val obstacles = obstaclePositions.map(pos => Obstacle.obstacle(pos, time))
    val quadTree = obstacles.map(_.shape).toQuadTree(GameState.worldBox)

    val (graph, inflatedEdges) = if (computeGraph) AntoineGraph(quadTree, Zombie.zombieRadius)
    else (new Graph(Vector(), Map()), Nil)

    gameState.copy(
      startTime = time,
      time = time,
      zombies = zombiesInfo.map(
        info => info.id -> new Zombie(info.id, time, info.pos, info.direction, info.moving)
      ).toMap,
      players = playersInfo.map(
        info => info.id -> new Player(info.id, time, info.pos, direction = 0, moving = false, info.colour)
      ).toMap,
      obstacles = obstacles,
      quadtree = quadTree,
      graph = graph,
      inflatedEdges = inflatedEdges
    )
  }

}

object InitialGameState {
  import upickle.default.{ReadWriter, macroRW}
  implicit val readWriter: ReadWriter[InitialGameState] = macroRW
}
