package main

import gamemanager.Game
import menus.MenuDisplay
import org.scalajs.dom


object Main {

  lazy val password: String = dom.document.cookie
    .split(";")
    .map(_.trim)
    .find(_.startsWith("enterGamePassword"))
    .get.split("=").last

  def main(args: Array[String]): Unit = {

    MenuDisplay
    Game

  }

}
