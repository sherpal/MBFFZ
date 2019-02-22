package gamestate

import physics.pathfinding.immutablegraph.Graph
import physics.quadtree.ShapeQT
import physics.shape.{BoundingBox, Segment}
import entities._
import gamestate.actions.{GameAction, InitialGameState, NewPlayerInfo, UpdatePlayerPos}

final case class GameState(
                            startTime: Long,
                            time: Long,
                            zombies: Map[Long, Zombie],
                            players: Map[Long, Player],
                            obstacles: List[Obstacle],
                            popStationContainer: PopStationContainer,
                            graph: Graph,
                            inflatedEdges: List[Segment],
                            quadtree: ShapeQT,
                            computationTimes: Map[String, Long]
                     ) {

  private def updateWith(action: GameAction): GameState = action(this)

  def apply(actions: Seq[GameAction]): GameState = actions.foldLeft(this)(_ updateWith _)

  def isLegalAction(action: GameAction): Boolean = action match {
    case UpdatePlayerPos(_, _, playerId, _, _, _) => players.isDefinedAt(playerId)
    case _ => true
  }

  def updatePlayer(newTime: Long, player: Player): GameState =
    copy(time = newTime, players = players + (player.id -> player))

  def removePlayer(newTime: Long, playerId: Long): GameState =
    copy(time = newTime, players = players - playerId)

  def updateZombies(newTime: Long, newZombies: Map[Long, Zombie], deletedZombies: List[Long]): GameState =
    copy(time = newTime, zombies = (zombies -- deletedZombies) ++ newZombies)

  def addPopStations(newTime: Long, popStations: Map[Long, ZombiePopStation]): GameState =
    copy(
      time = newTime,
      popStationContainer = new PopStationContainer(newTime, popStationContainer.popStations ++ popStations, newTime)
    )

  def stationsPop(newTime: Long, popStationIds: List[Long], zombieIds: List[Long]): GameState =
    {
      val poppedStations = popStationIds.map(popStationContainer.popStations)

      val newZombies = poppedStations.zip(zombieIds).map{
        case (station, id) => id -> new Zombie(id, newTime, station.pos, direction = 0, moving = false)
      }

      copy(
        time = newTime,
        zombies = zombies ++ newZombies.toMap,
        popStationContainer = new PopStationContainer(
          newTime,
          popStationContainer.popStations -- popStationIds,
          newTime
        )
      )
    }


}

object GameState {

  var worldBox: BoundingBox = BoundingBox(-400, -300, 400, 300)

  def startingGameState(nbrObstacles: Int = 5, playerColours: List[String]): InitialGameState = {

    val startTime: Long = new java.util.Date().getTime

    val (_, quadTree, obstaclePositions) =
      Obstacle.createSomeObstacles(nbrObstacles, startTime, ShapeQT(worldBox))

    val players = playerColours.map(Player.startingPlayer(startTime, quadTree, _))
      .map(player => player.id -> player).toMap

    InitialGameState(
      GameAction.id(),
      startTime,
      players.values.toList.map(player => NewPlayerInfo(player.id, player.pos, player.colour)),
      Nil,
      obstaclePositions
    )

  }

  def emptyGameState(graph: Graph, inflatedEdges: List[Segment], boundingBox: BoundingBox) = new GameState(
    0, 0, Map(), Map(), Nil, new PopStationContainer(0, Map(), 0),
    graph, inflatedEdges, ShapeQT(boundingBox), Map()
  )

  def emptyGameState(): GameState = emptyGameState(
    new Graph(Vector(), Map()), Nil, worldBox
  )

}
