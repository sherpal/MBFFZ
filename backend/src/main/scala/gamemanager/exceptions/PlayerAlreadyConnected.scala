package gamemanager.exceptions

final class PlayerAlreadyConnected(playerName: String) extends GameException(s"$playerName is already connected.")
