package physics.shape

import physics.Complex

trait Shape {
  val boundingBox: BoundingBox

  def collides(thisTranslation: Complex, thisRotation: Double,
               that: Shape, thatTranslation: Complex, thatRotation: Double): Boolean

  def intersectSegment(translation: Complex, rotation: Double, z1: Complex, z2: Complex): Boolean

  def contains(point: Complex): Boolean

  val radius: Double

}

object Shape {

  def segmentKind(z0: Complex, z1: Complex, z2: Complex): Int = {
    val det = (z1 - z0) crossProduct(z2 - z1)

    if (det > 0) { // convex corner
      if (z0.im < z1.im && z2.im < z1.im) 1 // no need of horizontal segment
      else if (z0.im > z1.im && z2.im > z1.im) 2 // full horizontal segment
      else if (z0.im < z1.im && z2.im > z1.im) 3 // horizontal segment to the left
      else 4 // horizontal segment to the right
    } else { // concave corner
      if (z0.im > z1.im && z2.im > z1.im) 1
      else if (z0.im < z1.im && z2.im < z1.im) 2
      else if (z0.im > z1.im && z2.im < z1.im) 3
      else 4
    }
  }

  def triangulateMonotonePolygon(vertices: Vector[Complex]): List[Triangle] = {
    def triangulationAcc(vertices: Vector[Complex], acc: List[Triangle]): List[Triangle] = {
      if (vertices.length == 3) Triangle(vertices.head, vertices(1), vertices.last) :: acc
      else {
        val convexCorner = vertices.indices.find(j => {
          val prev = if (j > 0) j - 1 else vertices.length - 1
          val next = if (j < vertices.length - 1) j + 1 else 0
          ((vertices(j) - vertices(prev)) crossProduct (vertices(next) - vertices(j))) > 0
        })


        convexCorner match {
          case None =>
            Console.err.print("Polygon was not Monotone")
            List()
          case Some(idx) =>
            val prev = if (idx > 0) idx - 1 else vertices.length - 1
            val next = if (idx < vertices.length - 1) idx + 1 else 0
            triangulationAcc(
              vertices.take(idx) ++ vertices.drop(idx + 1),
              Triangle(vertices(prev), vertices(idx), vertices(next)) :: acc
            )
        }
      }
    }

    triangulationAcc(vertices, List[Triangle]())
  }

  private final class Corner(val z: Complex, val next: Complex, val prev: Complex, val cornerIndex: Int) {
    val det: Double = (z - prev).crossProduct(next - z)
    def triangle: Triangle = Triangle(prev, z, next)
    def angle: Double = {
      val arg1 = (prev - z).arg
      val arg2 = (next - z).arg
      (if (arg1 < 0) arg1 + 2 * math.Pi else arg1) - (if (arg2 < 0) arg2 + 2 * math.Pi else arg2)
    }
    def isVertex(v: Complex): Boolean = v == z || v == next || v == prev
  }

  def earClipping(vertices: Vector[Complex]): List[Triangle] = {
    def earClippingAcc(vs: Vector[Complex], acc: List[Triangle]): List[Triangle] = {
      if (vs.length == 3) Triangle(vs.head, vs(1), vs.last) :: acc
      else {

        val corners = for (j <- vs.indices) yield new Corner(
          vs(j), vs(if (j == vs.length - 1) 0 else j + 1), vs(if (j == 0) vs.length - 1 else j-1), j
        )

        val (convex, reflex) = corners.partition(_.det > 0)
        val ears = convex.filter(v => !reflex.exists(c => (!v.isVertex(c.z)) && v.triangle.contains(c.z.re, c.z.im)))
        val process = ears.minBy(_.angle)

        val (beforeCorner, afterCorner) = vs.splitAt(process.cornerIndex)

        earClippingAcc(beforeCorner ++ afterCorner.tail, process.triangle :: acc)
      }
    }

    earClippingAcc(vertices, List()).sortWith((t1, t2) => t1.det > t2.det)
  }



  def regularPolygon(nbrSides: Int, radius: Double = 1): ConvexPolygon = new ConvexPolygon(
    (0 until nbrSides).map(j => radius * Complex.exp(Complex.i * 2 * math.Pi * j / nbrSides)).toVector
  )

  def translatedRegularPolygon(nbrSides: Int, radius: Double, translation: Complex): ConvexPolygon = new ConvexPolygon(
    (0 until nbrSides).map(j => translation + radius * Complex.exp(Complex.i * 2 * math.Pi * j / nbrSides)).toVector
  )


  /**
    * Returns the intersection point of the two segments
    * [x11 + i y11, x12 + i y12] and [x21 + i y21, x22 + i y22],
    * or None if it does not exist.
    */
  def intersectionPoint(x11: Double, y11: Double, x12: Double, y12: Double,
                        x21: Double, y21: Double, x22: Double, y22: Double): Option[Complex] = {
    val v1x = x12 - x11
    val v1y = y12 - y11
    val v2x = x22 - x21
    val v2y = y22 - y21

    val det = -v1x * v2y + v2x * v1y

    if (math.abs(det) < 1e-6) None
    else {
      val coef1 = (x11 - x21) * v2y - (y11 - y21) * v2x
      val coef2 = (x11 - x21) * v1y - (y11 - y21) * v1x

      if ((det >= 0 && coef1 <= det && coef2 <= det && coef1 >= 0 && coef2 >= 0) ||
        (coef1 <= 0 && coef2 <= 0 && coef1 >= det && coef2 >= det)) {
        Some(Complex(x11, y11) + coef1 / det * Complex(x12 - x11, y12 - y11))
      } else None
    }
  }

  /**
    * Returns whether the two segments
    * [x11 + i y11, x12 + i y12] and [x21 + i y21, x22 + i y22]
    * intersect.
    */
  def intersectingSegments(x11: Double, y11: Double, x12: Double, y12: Double,
                           x21: Double, y21: Double, x22: Double, y22: Double): Boolean = {

    val v1x = x12 - x11
    val v1y = y12 - y11
    val v2x = x22 - x21
    val v2y = y22 - y21

    val det = -v1x * v2y + v2x * v1y

    val coef1 = (x11 - x21) * v2y - (y11 - y21) * v2x
    val coef2 = (x11 - x21) * v1y - (y11 - y21) * v1x

    if (det >= 0) coef1 <= det && coef2 <= det && coef1 >= 0 && coef2 >= 0
    else coef1 <= 0 && coef2 <= 0 && coef1 >= det && coef2 >= det
  }

  /**
    * Returns whether the segments [z1, z2] and [w1, w2] intersect.
    */
  def intersectingSegments(z1: Complex, z2: Complex, w1: Complex, w2: Complex): Boolean =
    intersectingSegments(z1.re, z1.im, z2.re, z2.im, w1.re, w1.im, w2.re, w2.im)

  /**
    * Returns the intersection point of the two lines passing respectively through z1 and z2 with direction vectors
    * respectively dir1 and dir2.
    *
    * This is done by solving the system
    * z1 + t dir1 = z2 + t dir2
    */
  def linesIntersection(z1: Complex, z2: Complex, dir1: Complex, dir2: Complex): Complex = {
    val z = z2 - z1
    val t = (dir2.im * z.re - dir2.re * z.im) / (dir1 crossProduct dir2)
    z1 + t * dir1
  }

  def solveSecondDegree(a: Double, b: Double, c: Double): Option[(Double, Double)] = {
    val rho = b * b - 4 * a * c

    if (rho < 0) None
    else Some(((- b + math.sqrt(rho)) / (2 * a), (- b - math.sqrt(rho)) / (2 * a)))
  }

}
