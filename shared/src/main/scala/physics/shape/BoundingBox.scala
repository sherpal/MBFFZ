package physics.shape

import physics.Complex

final case class BoundingBox(left: Double, bottom: Double, right: Double, top: Double) {

  @inline def intersect(that: BoundingBox, translation: Complex = 0, translationThat: Complex = 0): Boolean = {
    math.max(this.left + translation.re, that.left + translationThat.re) <
      math.min(this.right + translation.re, that.right + translationThat.re) &&
      math.min(this.top + translation.im, that.top + translationThat.im) >
        math.max(this.bottom + translation.im, that.bottom + translationThat.im)
  }

  def contains(z: Complex): Boolean =
    z.re >= left && z.re <= right && z.im >= bottom && z.im <= top

  def size: Double = ((right - left) + (top - bottom)) / 2

  def vertices: Vector[Complex] = Vector(
    Complex(left, bottom), Complex(right, bottom),
    Complex(right, top), Complex(left, top)
  )

  def divide: List[BoundingBox] = {
    val semiWidth = (right - left) / 2
    val semiHeight = (top - bottom) / 2
    List(
      BoundingBox(left, bottom, left + semiWidth, bottom + semiHeight),
      BoundingBox(right - semiWidth, bottom, right, bottom + semiWidth),
      BoundingBox(left, top - semiHeight, left + semiWidth, top),
      BoundingBox(right - semiWidth, top - semiHeight, right, top)
    )
  }

  def width: Double = right - left
  def height: Double = top - bottom


}
