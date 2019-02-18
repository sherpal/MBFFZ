package gamemanager.exceptions

final class ColourAlreadyInUse(colourName: String) extends GameException(s"Colour $colourName is already chosen.")
