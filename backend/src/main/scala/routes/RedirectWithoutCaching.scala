package routes

import main.MBFFZ

/**
  * Redirects the client to the given url.
  */
object RedirectWithoutCaching {
  def apply(url: String): cask.Response = cask.Response(
    "", statusCode = 301, headers = MBFFZ.noCache :+ ("Location" -> url)
  )
}
