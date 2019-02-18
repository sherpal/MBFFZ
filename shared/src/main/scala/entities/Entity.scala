package entities

trait Entity {

  val id: Long

  val time: Long

}

object Entity {

  private var lastId: Long = 0

  def newId(): Long = {
    lastId += 1
    lastId
  }

}
