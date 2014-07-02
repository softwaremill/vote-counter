package com.softwaremill.votecounter.web

import java.security.{KeyStore, SecureRandom}
import java.util.Collections
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.{ActorRef, ActorSystem}
import akka.io.IO
import com.softwaremill.votecounter.config.{BaseConfig, VoteCounterConfig}
import com.softwaremill.votecounter.util.Resources
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.StrictLogging
import spray.can.Http
import spray.can.server.ServerSettings
import spray.io.ServerSSLEngineProvider

import collection.JavaConverters._

trait SslConfiguration extends StrictLogging {

  protected val keystorePasswordOpt: Option[String]

  import com.softwaremill.votecounter.web.SslConfiguration._

  lazy val sslEngineProvider = ServerSSLEngineProvider { engine =>
    engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA", "TLS_DHE_DSS_WITH_AES_128_CBC_SHA"))
    engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
    engine
  }


  // See https://github.com/spray/spray/blob/master/examples/spray-can/simple-http-server/src/main/scala/spray/examples/MySslConfiguration.scala
  implicit lazy val sslContext: SSLContext = {
    val password = keystorePasswordOpt.get /* will fail by design if password is not available */

    val keystoreStream = Resources.inputStreamInClasspath(KeystoreResourcePath)

    val keyStore = KeyStore.getInstance("jks")
    keyStore.load(keystoreStream, password.toCharArray)

    logger.debug(s"Available aliases in the keystore: ${Collections.list(keyStore.aliases()).asScala.mkString(",")}.")

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)

    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom())

    context
  }

  val sslConfig = ServerSettings(ConfigFactory.defaultReference(getClass.getClassLoader)).copy(sslEncryption = true)
}

object SslConfiguration {
  val KeystoreResourcePath = "keystore.jks"
}

class SslServer(webHandler: ActorRef, config: VoteCounterConfig, protected implicit val actorSystem: ActorSystem)
  extends StrictLogging with SslConfiguration {

  def start() {
    if (!config.sslEnabled) {
      logger.debug("Not starting HTTPS server. Not enabled.")
    } else if (!checkIfAvailable()) {
      logger.error("Could not start HTTPS server.")
    } else {
      IO(Http) ! Http.Bind(webHandler, interface = "0.0.0.0", port = 8090,
        settings = Some(sslConfig))(sslEngineProvider)
    }
  }

  private def checkIfAvailable(): Boolean = {
    var isOk_? = true

    if (config.sslKeystorePassword.isEmpty) {
      logger.warn(s"SSL keystore password not available. Please set ${BaseConfig.SslKeystorePasswordKey} " +
        s"in your application.conf.")
      isOk_? = false
    }

    if (!Resources.existsInClasspath("keystore.jks")) {
      logger.warn(s"SSL keystore file ${SslConfiguration.KeystoreResourcePath} not found in classpath.")
      isOk_? = false
    }

    isOk_?
  }

  override protected val keystorePasswordOpt: Option[String] = config.sslKeystorePassword
}

