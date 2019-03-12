package physics.quadtree

import physics.Complex
import physics.shape.{BoundingBox, Polygon, Shape}

/**
  * QuadTree to check Bodies to obstacles collision.
  */
final class ShapeQT private (
                              subTrees: List[ShapeQT],
                              val shapes: List[Polygon],
                              val boundingBox: BoundingBox
                            ) {

  private val threshold: Double = 50.0

  private val rectangle: Polygon = Polygon(boundingBox.vertices)

  private def divide: List[ShapeQT] = boundingBox.divide.map(new ShapeQT(Nil, Nil, _))

  private def shouldContain(shape: Shape): Boolean =
    shape.collides(0, 0, rectangle, 0, 0)

  private def couldContain(shape: Shape, translation: Complex): Boolean =
    boundingBox.intersect(shape.boundingBox, 0, translation)

  private def shouldDivide: Boolean = threshold < boundingBox.size

  def :+(shape: Polygon): ShapeQT =
    if (!shouldContain(shape)) this
    else {
      val newShapes = shape +: shapes
      if (!shouldDivide) new ShapeQT(Nil, newShapes, boundingBox)
      else if (subTrees.nonEmpty) new ShapeQT(subTrees.map(_ :+ shape), newShapes, boundingBox)
      else new ShapeQT(divide.map(shapeQT => newShapes.foldLeft(shapeQT)({
        case (qt, s) => qt :+ s
      })), newShapes, boundingBox)
    }

  def collides(shape: Shape, translation: Complex, rotation: Double): Boolean =
    if (subTrees.isEmpty) shapes.exists(_.collides(0, 0, shape, translation, rotation))
    else subTrees.filter(_.couldContain(shape, translation)).exists(_.collides(shape, translation, rotation))

  def contains(shape: Shape): Boolean =
    if (subTrees.isEmpty) shapes.contains(shape)
    else subTrees.filter(_.couldContain(shape, 0)).exists(_.contains(shape))

  /**
    * Returns whether there exists a shape in the quad tree that contains the point.
    */
  def contains(point: Complex): Boolean =
    if (subTrees.isEmpty) shapes.exists(_.contains(point))
    else subTrees.find(_.boundingBox.contains(point)) match {
      case Some(child) => child.contains(point)
      case None => false
    }

  def isEmpty: Boolean = shapes.isEmpty

  def nonEmpty: Boolean = shapes.nonEmpty

  def size: Int = shapes.size

  def toProtoString: String =
  s"""BorderSize: $boundingBox
    |Size: $size
    |SubTrees: ${subTrees.map(_.toProtoString).map(_.split("\n").map(" " + _).mkString("\n")).mkString("\n")}
  """.stripMargin

}

object ShapeQT {

  private def emptyShapeQT(boundingBox: BoundingBox): ShapeQT =
    new ShapeQT(Nil, Nil, boundingBox)

  private def emptyShapeQT(left: Double, bottom: Double, right: Double, top: Double): ShapeQT =
    emptyShapeQT(BoundingBox(left, bottom, right, top))

  def apply(left: Double, bottom: Double, right: Double, top: Double, shapes: Polygon*): ShapeQT =
    shapes.toQuadTree(left, bottom, right, top)

  def apply(boundingBox: BoundingBox, shapes: Polygon*): ShapeQT =
    shapes.toQuadTree(boundingBox.left, boundingBox.bottom, boundingBox.right, boundingBox.top)

  implicit class TraversableToQuadTree(seq: Traversable[Polygon]) {

    def toQuadTree(left: Double, bottom: Double, right: Double, top: Double): ShapeQT =
      seq.foldLeft(emptyShapeQT(left, bottom, right, top))({ case (qt, shape) => qt :+ shape})

    def toQuadTree(boundingBox: BoundingBox): ShapeQT =
      toQuadTree(boundingBox.left, boundingBox.bottom, boundingBox.right, boundingBox.top)

  }

}