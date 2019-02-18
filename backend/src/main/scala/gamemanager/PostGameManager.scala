package gamemanager

import gamestate.actions.GameEnd

final class PostGameManager(val gameEnd: GameEnd, val orderOfDeaths: List[(String, Long)]) {

}
