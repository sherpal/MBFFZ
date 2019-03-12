package routes

/**
  * These routes serve static files to the client, mainly the compiled js code.
  */
object StaticFiles extends cask.Routes {

  /**
    * Static files need to be stored in the resources of the backend, in the frontend folder.
    */
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
