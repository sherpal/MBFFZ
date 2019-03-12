package utils

/**
  * Represents a colour for the Players.
  * @param red in [0, 255]
  * @param green in [0, 255]
  * @param blue in [0, 255]
  */
final case class Colour(red: Int, green: Int, blue: Int) {

  def toInt: Int = blue + green * 256 + red * 256 * 256

  def cssString: String = s"rgb($red,$green,$blue)"

}
