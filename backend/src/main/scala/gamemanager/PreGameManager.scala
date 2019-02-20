package gamemanager

import entities.Player
import gamemanager.exceptions._

import scala.collection.mutable
import scala.util.Random

object PreGameManager {

  /** Map from player names to their colour. */
  private val _players: mutable.Map[String, String] = mutable.Map()
  /** Map from passwords to player names. */
  private val passwords: mutable.Map[String, String] = mutable.Map()
  private var _headPlayer: Option[String] = None

  def usedColours: Set[String] = _players.values.toSet

  def isHead(playerName: String): Boolean = _headPlayer.get == playerName

  def players: Map[String, String] = _players.toMap

  def addPlayer(playerName: String, colourName: String): String = this.synchronized {
    if (Manager.playing)
      throw new GameIsPlaying

    if (!Player.playerColours.isDefinedAt(colourName))
      throw new ColourDoesNotExist(colourName)

    if (_players.isDefinedAt(playerName))
      throw new PlayerAlreadyConnected(playerName)

    if (_players.values.toList.contains(colourName))
      throw new ColourAlreadyInUse(colourName)

    def createPassword(): String = {
      val nextTry = Random.nextInt().toString
      if (passwords.isDefinedAt(nextTry)) createPassword()
      else nextTry
    }

    val password = createPassword()

    if (_players.isEmpty)
      _headPlayer = Some(playerName)
    _players += (playerName -> colourName)
    passwords += (password -> playerName)

    password
  }

  def removePlayer(password: String): Unit = this.synchronized {
    passwords.get(password) match {
      case Some(plrName) =>
        passwords -= password
        _players -= plrName

        if (passwords.nonEmpty && isHead(plrName)) {
          Manager.server.sendTextToClient(
            "you're the head",
            Manager.server.getClient(passwords.head._1).get
          )
        } else {
          println("A player left.")
        }
        Manager.server.broadcastText("player list update")
      case None =>
    }

  }

  def passwordExists(pw: String): Boolean = passwords.isDefinedAt(pw)

  /**
    * Launch the game.
    * There must be at least 2 people in the game.
    * @param passwordLaunching password of the person requiring the game to be launched.
    *                          Only the head of the game can launch the game.
    */
  def startGame(passwordLaunching: String): Unit = this.synchronized {
    if (_players.size < 2)
      throw new NotEnoughPlayers(_players.size)
    if (!isPlaying(passwordLaunching) || !isHead(playerName(passwordLaunching)))
      throw new UnauthorizedToLaunch
    if (Manager.playing)
      throw new GameIsPlaying

    Manager.startGame(playersInfo = players, passwords = passwords.toMap.map(_.swap))
  }

  def playerName(password: String): String = passwords(password)

  def isPlaying(password: String): Boolean = passwords.isDefinedAt(password)

  def clear(): Unit = {
    _players.clear()
    passwords.clear()
    _headPlayer = None
  }

}
