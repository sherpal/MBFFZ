package utils

final case class Colour(red: Int, green: Int, blue: Int) {

  def toInt: Int = blue + green * 256 + red * 256 * 256

  def cssString: String = s"rgb($red,$green,$blue)"

}
