package com.softwaremill.votecounter.web

import java.io.File

import spray.routing._
import spray.routing.directives.ContentTypeResolver

trait JarOrFileResolver {
  def getWebappFile(path: String): Route
}

trait WebappPathDirectives extends JarOrFileResolver with HttpService {

  protected val cacheFiles: Boolean

  private def runningFromJar_? = {
    try {
      val cs = getClass.getProtectionDomain.getCodeSource
      cs.getLocation.toURI.getPath.endsWith(".jar")
    } catch {
      case e: Exception => true
    }
  }

  private lazy val jarOrFileResolver: JarOrFileResolver = {
    if (runningFromJar_?)
      ResourceResolver
    else
      FileResolver
  }

  override def getWebappFile(path: String): Route =
    jarOrFileResolver.getWebappFile(path)

  object ResourceResolver extends JarOrFileResolver {
    private val ResourcePathPrefix = "webapp"

    override def getWebappFile(path: String) = {
      getFromResource(ResourcePathPrefix + "/" + path)
    }
  }

  object FileResolver extends JarOrFileResolver {
    private val FilePathPrefix = "/Users/maciejb/Development/vote-counter/web/webapp"

    override def getWebappFile(path: String) = {
      val fullPath = FilePathPrefix + File.separator + path
      getFromFile(fullPath)(RoutingSettings.default.copy(fileGetConditional = cacheFiles),
        ContentTypeResolver.Default,
        actorRefFactory)
    }
  }

}