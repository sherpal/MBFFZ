package gamemanager

import gamestate.actions.GameEnd

/**
  * Contains the information of the last game.
  * @param gameEnd game information
  * @param orderOfDeaths player deaths information
  */
final class PostGameManager(val gameEnd: GameEnd, val orderOfDeaths: List[(String, Long)])
