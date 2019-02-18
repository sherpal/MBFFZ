package gamemanager.exceptions

final class ColourDoesNotExist(colourName: String) extends GameException(s"Colour $colourName does not exist.")
