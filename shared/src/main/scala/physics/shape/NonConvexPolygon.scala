package physics.shape

import physics.Complex

final class NonConvexPolygon(val vertices: Vector[Complex]) extends Polygon {
  val triangulation: List[Triangle] = Shape.earClipping(vertices)
}
