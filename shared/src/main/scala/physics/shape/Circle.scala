package physics.shape

import physics.Complex

final class Circle(val radius: Double) extends Curved {

  val boundingBox: BoundingBox = BoundingBox(- radius, - radius, radius, radius)

  def intersectSegment(translation: Complex, x0: Double, y0: Double, x1: Double, y1: Double): Boolean = {
    val vx = x1 - x0
    val vy = y1 - y0
    val zx = x0 - translation.re
    val zy = y0 - translation.im

    // first checking in one edge is in the disk
    zx * zx + zy * zy <= radius * radius || {
      // then checking if there is an intersection
      val intersectionPoints = Shape.solveSecondDegree(
        vx * vx + vy * vy,
        2 * (zx * vx + zy * vy),
        zx * zx + zy * zy - radius * radius
      )

      intersectionPoints match {
        case None =>
          // no intersection between the line and the disk
          false
        case Some((lambda1, lambda2)) =>
          // intersection between the line and the disk, checking if it is in the segment
          (0 <= lambda1 && lambda1 <= 1) || (0 <= lambda2 && lambda2 <= 1)
      }
    }
  }

  def overlapTriangle(t: Triangle): Boolean = {
    t.contains(0, 0) || intersectSegment(t.x0, t.y0, t.x1, t.y1) ||
      intersectSegment(t.x0, t.y0, t.x2, t.y2) || intersectSegment(t.x1, t.y1, t.x2, t.y2)
  }

  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean = that match {
    case that: Circle =>
      val dx = thisTranslation.re - thatTranslation.re
      val dy = thisTranslation.im - thatTranslation.im
      dy * dy + dx * dx <= (radius + that.radius) * (radius + that.radius)
    case that: Polygon => that.collides(thatTranslation, thatRotation, this, thisTranslation, thisRotation)
  }

  def contains(point: Complex): Boolean = point.modulus <= radius

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean = {
    val dir = z2 - z1
    val dirMod = dir.modulus
    val unitVec = dir / dirMod

    val scalarProduct = (translation - z1) scalarProduct unitVec

    0 <= scalarProduct && scalarProduct <= dirMod && {
      val projection = z1 + unitVec * scalarProduct
      val orthogonalProjection = translation - projection
      orthogonalProjection.modulus2 < radius * radius
    }

  }

}
