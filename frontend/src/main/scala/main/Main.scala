package main

import org.scalajs.dom
import websockets.Communicator


object Main {

  lazy val password: String = dom.document.cookie
    .split(";")
    .map(_.trim)
    .find(_.startsWith("enterGamePassword"))
    .get.split("=").last

  def main(args: Array[String]): Unit = {

    Communicator(password)

  }

}
