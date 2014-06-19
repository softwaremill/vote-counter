package com.softwaremill.votecounter.infrastructure

import com.softwaremill.macwire.Macwire
import com.softwaremill.votecounter.config.VoteCounterConfig
import com.typesafe.config.ConfigFactory
import com.softwaremill.votecounter.h2.{TestDataPopulator, DBInitializer, SQLDatabase}
import com.softwaremill.votecounter.db.{FlagDao, AppFlags, DeviceDao, VoteDao}
import akka.actor.{Props, ActorSystem}
import com.softwaremill.thegarden.lawn.shutdownables._
import com.softwaremill.votecounter.web.VoteCounterWebService


trait ConfigModule {
  lazy val config = new VoteCounterConfig {
    override def rootConfig = ConfigFactory.load()
  }
}

trait DBModule extends Macwire with DefaultShutdownHandlerModule with ConfigModule {
  lazy val database = SQLDatabase.createEmbedded(config) onShutdown { db =>
    db.close()
  }

  lazy val voteDao = wire[VoteDao]
  lazy val deviceDao = wire[DeviceDao]
  lazy val flagDao = wire[FlagDao]

  lazy val dbInitializer = wire[DBInitializer]
}

trait CoreModule extends Macwire with DefaultShutdownHandlerModule
with ConfigModule with DBModule {

  lazy val actorSystem = ActorSystem("vc-main") onShutdown { actorSystem =>
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

  lazy val testDataPopulator = wire[TestDataPopulator]
  lazy val flags = wire[AppFlags]

}

trait Beans extends CoreModule with ShutdownOnJVMTermination {
  val webHandler = actorSystem.actorOf(Props(classOf[VoteCounterWebService], this), "vote-service")
}

object Beans extends Beans
