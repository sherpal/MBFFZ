package physics.shape

import physics.Complex

trait Polygon extends Shape {
  val vertices: Vector[Complex]
  val triangulation: List[Triangle]

  val center: Complex = vertices.sum / vertices.length
  val radius: Double = math.sqrt(vertices.map(z => (z - center).modulus2).max)


  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean = {
    if (!boundingBox.intersect(that.boundingBox, thisTranslation, thatTranslation)) false
    else {
      that match {
        case that: Polygon =>
          this.triangulation.exists(t => that.triangulation.exists(thatT => {
            t.overlap(thatT, thisRotation, thisTranslation, thatRotation, thatTranslation)
          }))
        case that: Circle =>
          this.triangulation.exists(t => t.overlapDisk(
            that, thatTranslation, thisRotation, thisTranslation
          ))
      }
    }
  }

  val boundingBox: BoundingBox =
    BoundingBox(center.re - radius, center.im - radius, center.re + radius, center.im + radius)

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean =
    triangulation.exists(triangle => {
      val actualVertices = triangle.vertices.map(_ * Complex.rotation(rotation) + translation)
      triangle.contains(z1.re, z1.im, rotation, translation) ||
        triangle.contains(z2.re, z2.im, rotation, translation) ||
        actualVertices.zip(actualVertices.tail :+ actualVertices.head).exists({case (w1, w2) =>
          Shape.intersectingSegments(w1.re, w1.im, w2.re, w2.im, z1.re, z1.im, z2.re, z2.im)
        })
    })

  def contains(point: Complex): Boolean =
    boundingBox.contains(point) && triangulation.exists(_.contains(point.re, point.im))

  def edges: Vector[Segment] = vertices.zip(vertices.tail :+ vertices(0)).map(Segment.tupled)

  /**
    * Returns the polygon where all the edges are translated radius away to the exterior.
    */
  def inflate(radius: Double): Polygon = Polygon(inflateWithoutPolygon(radius))

  /**
    * Returns the vertices where all the edges are translated radius away to the exterior.
    */
  def inflateWithoutPolygon(radius: Double): Vector[Complex] = {
    val triplets = vertices.indices.map(j =>
      (
        vertices(j),
        vertices(if (j == vertices.length - 1) 0 else j + 1),
        vertices(if (j == 0) vertices.length - 1 else j - 1)
      )
    ).toVector
    for {
      (vertex, next, previous) <- triplets
    } yield {
      val dir1 = (vertex - previous).normalized
      val dir2 = (next - vertex).normalized
      val vertex1 = vertex - radius * dir1.orthogonal
      val vertex2 = vertex - radius * dir2.orthogonal
      Shape.linesIntersection(
        vertex1, vertex2, dir1, dir2
      )
    }
  }

}

object Polygon {
  def apply(vertices: Vector[Complex], convex: Boolean = false): Polygon =
    if (convex) new ConvexPolygon(vertices) else new NonConvexPolygon(vertices)
}

