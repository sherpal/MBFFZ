package physics.pathfinding.immutablegraph

import physics.Complex
import physics.quadtree.ShapeQT
import physics.shape.{Polygon, Segment, Shape}

object AntoineGraph {

  private implicit class ShapeWithEdge(shape: Shape) {

    def shapeVertices: Vector[Complex] = shape match {
      case polygon: Polygon =>
        polygon.vertices
    }

    def edges: Vector[Segment] = shape match {
      case polygon: Polygon =>
        polygon.vertices.zip(polygon.vertices.tail :+ polygon.vertices(0))
          .map(Segment.tupled)
    }


  }

  def addVertices(
                   vertices: List[Complex],
                   neighboursMap: Map[Complex, List[Complex]],
                   quadTree: ShapeQT,
                   inflatedEdges: Traversable[Segment],
                   forceInclusion: Boolean = false
                 ): Map[Complex, List[Complex]] =
    vertices.foldLeft(neighboursMap)({
      case (map, vertex) => addVertex(vertex, map, quadTree, inflatedEdges, forceInclusion)
    })

  def addVertex(
                 vertex: Complex,
                 neighboursMap: Map[Complex, List[Complex]],
                 quadTree: ShapeQT,
                 inflatedEdges: Traversable[Segment],
                 forceInclusion: Boolean = false
               ): Map[Complex, List[Complex]] = {
    if (neighboursMap.isDefinedAt(vertex)) neighboursMap
    else {
      val possibleSegments = inflatedEdges.filterNot(_.hasEdge(vertex))
      val connectedTo = (for {
        v <- neighboursMap.keys
        if !possibleSegments.filterNot(_.hasEdge(v))
          .exists(segment => Shape.intersectingSegments(vertex, v, segment.z1, segment.z2))
        if !quadTree.contains((vertex + v) / 2)
      } yield v).toList

      val finalConnectedTo = if (connectedTo.isEmpty && forceInclusion && neighboursMap.nonEmpty)
        List(neighboursMap.keys.minBy(z => (vertex - z).modulus2))
      else connectedTo

      (neighboursMap ++ finalConnectedTo.map(v => v -> (vertex +: neighboursMap(v)))) + (vertex -> finalConnectedTo)
    }
  }

  def apply(quadTree: ShapeQT, radius: Double): (Graph, List[Segment]) = {
    val startTime = new java.util.Date().getTime

    val obstacles = quadTree.shapes

    val verticesPerObstacles = obstacles.map(_.inflateWithoutPolygon(radius))

    val inflatedEdges = obstacles.map(_.inflateWithoutPolygon(radius * 0.9))
      .flatMap(vertices => vertices.zip(vertices.tail :+ vertices(0)).map(Segment.tupled))

    val allVertices = verticesPerObstacles.flatten.toVector

    val neighboursMapOneWay: Map[Complex, List[Complex]] =
      allVertices.indices.map(idx => {
        val v1 = allVertices(idx)
        val possibleSegments = inflatedEdges.filterNot(_.hasEdge(v1))
        val connectedTo: List[Complex] = {
          for {
            idx2 <- idx + 1 until allVertices.length
            v2 = allVertices(idx2)
            if !possibleSegments.filterNot(_.hasEdge(v2))
              .exists(segment => Shape.intersectingSegments(v1, v2, segment.z1, segment.z2))
            if !quadTree.contains((v1 + v2) / 2)
          } yield v2
        }.toList
        v1 -> connectedTo
      }).toMap

    val neighboursMap = neighboursMapOneWay ++ neighboursMapOneWay.toList.flatMap({
      case (z, zs) => zs.map(_ -> z)
    }).groupBy(_._1).mapValues(_.map(_._2))

    println(s"It took ${new java.util.Date().getTime - startTime} ms to compute")

    val allEdges = neighboursMap.toList.flatMap({
      case (z, zs) => zs.map(z -> _)
    })

    println(s"there are ${allEdges.length} edges in the graph.")

    (new Graph(allVertices, neighboursMap), inflatedEdges)
  }

}
