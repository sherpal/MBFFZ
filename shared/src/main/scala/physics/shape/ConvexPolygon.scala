package physics.shape

import physics.Complex

final class ConvexPolygon(val vertices: Vector[Complex]) extends Polygon {

//  if (scala.scalajs.LinkingInfo.developmentMode) {
    // checking if vertices are counterclockwise
//    val edges = ((vertices.last, vertices(0)) +: vertices.zip(vertices.tail)).map(elem => elem._1 - elem._2)
//    assert(edges.zip(edges.tail).forall(elem => elem._1.crossProduct(elem._2) > 0), vertices.mkString(", "))
//  }

  val triangulation: List[Triangle] = {
    (for (j <- 1 until vertices.length - 1) yield Triangle(vertices(0), vertices(j), vertices(j + 1))).toList
  }

}
