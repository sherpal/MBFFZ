package gamemanager.exceptions

final class NotEnoughPlayers(playerCount: Int)
  extends GameException(s"There should be at least 2 players to play (currently $playerCount).")
