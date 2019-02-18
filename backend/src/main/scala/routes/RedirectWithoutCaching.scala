package routes

import main.MBFFZ

object RedirectWithoutCaching {
  def apply(url: String): cask.Response = cask.Response(
    "", statusCode = 301, headers = MBFFZ.noCache :+ ("Location" -> url)
  )
}
