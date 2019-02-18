package entities

import physics.Complex
import physics.quadtree.ShapeQT

trait MovingBody extends Body {

  val direction: Double
  val speed: Double
  val moving: Boolean

  def currentPosition(deltaTime: Long, dir: Double = direction): Complex =
    if (moving) pos + (speed * deltaTime / 1000) * Complex.rotation(dir) else pos

  def lastValidPos(deltaTime: Long, quadTree: ShapeQT, dir: Double = direction, step: Double = 2): Complex = {
    val target = currentPosition(deltaTime, dir)
    val distance2 = (target - pos).modulus2
    if (target == pos) target
    else {
      val towards = (target - pos).normalized

      def tryNext(currentTry: Complex): Complex = {
        val nextTry = currentTry + step * towards
        if (quadTree.collides(shape, nextTry, rotation)) currentTry
        else if ((currentTry - pos).modulus2 >= distance2) target
        else tryNext(nextTry)
      }

      tryNext(pos)
    }
  }

}
