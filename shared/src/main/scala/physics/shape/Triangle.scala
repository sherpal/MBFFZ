package physics.shape

import physics.Complex

final class Triangle(val x0: Double, val y0: Double,
                                    val x1: Double, val y1: Double,
                                    val x2: Double, val y2: Double) {

  val vertices: Vector[Complex] = Vector(Complex(x0, y0), Complex(x1, y1), Complex(x2, y2))

  val det: Double = Complex(x1 - x0, y1 - y0) crossProduct Complex(x2 - x0, y2 - y0)

//  if (scala.scalajs.LinkingInfo.developmentMode) {
//    assert(det > 0, "triangle is not counterclockwise oriented")
//  }

  @inline def overlapDisk(that: Circle, center: Complex, rot: Double = 0, translation: Complex = 0): Boolean = {
    val transformedVertices = vertices.map(_ * Complex.rotation(rot) + translation)
    that.intersectSegment(
      center, transformedVertices(0).re, transformedVertices(0).im,
      transformedVertices(1).re, transformedVertices(1).im
    ) ||
      that.intersectSegment(
        center, transformedVertices(1).re, transformedVertices(1).im,
        transformedVertices(2).re, transformedVertices(2).im
      ) ||
      that.intersectSegment(
        center, transformedVertices(2).re, transformedVertices(2).im,
        transformedVertices(0).re, transformedVertices(0).im
      )
  }

  @inline def overlap(that: Triangle, rot: Double = 0, trans: Complex = 0,
              rotThat: Double = 0, transThat: Complex = 0): Boolean = {
    val cs = effectiveCoordinates(rot, trans)
    val csThat = that.effectiveCoordinates(rotThat, transThat)

    Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
      csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(0).re, csThat(0).im, csThat(2).re, csThat(2).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(1).re, cs(1).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(0).re, cs(0).im, cs(2).re, cs(2).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      Shape.intersectingSegments(cs(2).re, cs(2).im, cs(1).re, cs(1).im,
        csThat(2).re, csThat(2).im, csThat(1).re, csThat(1).im) ||
      this.contains(csThat(0).re, csThat(0).im, cs) ||
      this.contains(csThat(1).re, csThat(1).im, cs) ||
      this.contains(csThat(2).re, csThat(2).im, cs) ||
      that.contains(cs(0).re, cs(0).im, csThat) ||
      that.contains(cs(1).re, cs(1).im, csThat) ||
      that.contains(cs(2).re, cs(2).im, csThat)
  }

  def effectiveCoordinates(rotation: Double, translation: Complex): Vector[Complex] =
    vertices.map(z => z * Complex.rotation(rotation) + translation)

  def contains(x: Double, y: Double, rotation: Double = 0, translation: Complex  = 0): Boolean = {
    val cs = if (rotation == 0 && translation == Complex(0,0)) vertices else
      effectiveCoordinates(rotation, translation)
    contains(x, y, cs)
  }

  def contains(x: Double, y: Double, cs: Vector[Complex]): Boolean = {
    val coef1 = (cs(2).im - cs(0).im) * (x - cs(0).re) - (cs(2).re - cs(0).re) * (y - cs(0).im)
    val coef2 = (cs(1).re - cs(0).re) * (y - cs(0).im) - (cs(1).im - cs(0).im) * (x - cs(0).re)

    coef1 >= 0 && coef2 >= 0 && coef1 + coef2 <= det
  }
}

object Triangle {
  def apply(v0: Complex, v1: Complex, v2: Complex): Triangle = new Triangle(
    v0.re, v0.im, v1.re, v1.im, v2.re, v2.im
  )
}

