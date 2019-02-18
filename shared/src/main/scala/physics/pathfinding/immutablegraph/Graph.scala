package physics.pathfinding.immutablegraph

import physics.Complex

final class Graph(
                   val vertices: Vector[Complex],
                   val neighboursMap: Map[Complex, List[Complex]]
                 ) {

  def a_*(start: Complex, end: Complex,
          heuristicFunction: (Complex, Complex) => Double): Option[Seq[Complex]] = {

    def reconstructPath(cameFromMap: Map[Complex, Complex]): List[Complex] = {
      def withAccumulator(currentPath: List[Complex]): List[Complex] =
        if (currentPath.head == start) currentPath
        else withAccumulator(cameFromMap(currentPath.head) +: currentPath)

      withAccumulator(List(end))
    }

    def h(z: Complex): Double = heuristicFunction(z, end)
    def d(z1: Complex, z2: Complex): Double = !(z1 - z2)

    final case class Score(f: Double, g: Double)
    val defaultScore = Score(Double.MaxValue, Double.MaxValue)

    def exploration(openSet: Map[Complex, Score], closedSet: Set[Complex], cameFromMap: Map[Complex, Complex]):
    Map[Complex, Complex] =
      if (openSet.isEmpty) Map()
      else {
        val (currentVertex, currentVertexScore) = openSet.minBy(_._2.f)

        if (currentVertex == end) cameFromMap
        else {
          val newOpenSet = openSet - currentVertex
          val newClosedSet = closedSet + currentVertex

          val newElementsInOpenSet = neighboursMap(currentVertex).filterNot(closedSet.contains)
            .map(neighbour => (
              neighbour,
              currentVertexScore.g + d(currentVertex, neighbour),
              openSet.getOrElse(neighbour, defaultScore))
            )
            .filter(triplet => triplet._2 < triplet._3.g)
            .map({
              case (neighbour, tentativeScoreG, _) =>
                neighbour -> Score(tentativeScoreG + h(neighbour), tentativeScoreG)
            })
            .toMap

          exploration(
            newOpenSet ++ newElementsInOpenSet,
            newClosedSet,
            cameFromMap ++ newElementsInOpenSet.keys.map(_ -> currentVertex).toMap
          )
        }
      }

    val explorationResult = exploration(
      Map(start -> Score(h(start), 0)),
      Set(),
      Map()
    )

    if (explorationResult.isEmpty) None else Some(reconstructPath(explorationResult))

  }

}

object Graph {

  implicit class Vertex(z: Complex) {
    def neighbours(implicit neighboursMap: Map[Complex, List[Complex]]): List[Complex] = neighboursMap(z)
  }

}