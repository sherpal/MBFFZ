package routes

object StaticFiles extends cask.Routes {

  private val defaultPath: String = "backend/src/main/resources/frontend/"

  @cask.get("/app", subpath = true)
  def scriptFile(request: cask.Request): cask.Response = {
    val path = request.remainingPathSegments.mkString("/")
    cask.Redirect("/inside-app/" + path)
  }

  @cask.staticFiles("/inside-app/:path")
  def scriptFromInside(path: String): String = defaultPath + path


  initialize()
}
