package gamemanager

import gamestate.actions.GameEnd

/**
  * Manages the state of the server.
  * It is either playing, in which case it ignores any messages from the outside, and only manages
  * the game, or it is not, in which case it waits for new players to come.
  */
object Manager {

  final val server: GameServer = new GameServer
  server.startCheckConnection()

  sealed trait State
  case object PreGameState extends State
  case object PlayingState extends State

  private var _state: State = PreGameState
  def state: State = _state

  def setState(state: State): Unit =
    _state = state

  def playing: Boolean = _state == PlayingState

  private var _gameManager: Option[GameManager] = None
  private var _postGameManager: Option[PostGameManager] = None

  def startGame(playersInfo: Map[String, String], passwords: Map[String, String]): Unit = this.synchronized {
    _gameManager = Some(new GameManager(playersInfo, passwords))
    _gameManager.get.startThread()
  }

  def endGame(gameEnd: GameEnd, orderOfDeaths: List[(String, Long)]): Unit = {
    Manager.setState(Manager.PreGameState)
    _postGameManager = Some(new PostGameManager(gameEnd, orderOfDeaths))
    _gameManager = None
    PreGameManager.clear()
  }

  @inline def postGameManager: Option[PostGameManager] = _postGameManager
  @inline def gameManager: Option[GameManager] = _gameManager


}
