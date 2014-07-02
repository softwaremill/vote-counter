package com.softwaremill.votecounter.infrastructure

import com.softwaremill.macwire.Macwire
import com.softwaremill.votecounter.config.VoteCounterConfig
import com.softwaremill.votecounter.voting.{VotingResultAggregator, VoteRequestProcessor}
import com.typesafe.config.ConfigFactory
import com.softwaremill.votecounter.h2.{ConferenceDataInitializer, TestDataPopulator, DBInitializer, SQLDatabase}
import com.softwaremill.votecounter.db._
import akka.actor.{Props, ActorSystem}
import com.softwaremill.thegarden.lawn.shutdownables._
import com.softwaremill.votecounter.web.{SslServer, VoteCounterWebService}
import com.softwaremill.votecounter.confitura._


trait ConfigModule {
  lazy val config = new VoteCounterConfig {
    override def rootConfig = ConfigFactory.load()
  }
}

trait DBModule extends Macwire with DefaultShutdownHandlerModule with ConfigModule {
  lazy val database = SQLDatabase.createEmbedded(config) onShutdown { db =>
    db.close()
  }

  lazy val voteDao = wire[VotesDao]
  lazy val deviceDao = wire[DevicesDao]
  lazy val flagDao = wire[FlagsDao]
  lazy val roomsDao = wire[RoomsDao]
  lazy val talksDao = wire[TalksDao]

  lazy val dbInitializer = wire[DBInitializer]
}

trait ConfituraModule extends Macwire with ConfigModule {
  lazy val agendaFileReader = wire[AgendaFileReader]

  lazy val roomsProvider: RoomsProvider = wire[ConfituraRooms]
  lazy val talksProvider: TalksProvider = new ConfituraTalks(agendaFileReader,
    config.conferenceDate, config.conferenceTimeZone)
  lazy val devicesProvider: DevicesProvider = wire[ConfituraDevices]
}

trait VotesModule extends Macwire with DBModule {

  lazy val votesRequestProcessor = wire[VoteRequestProcessor]

  lazy val votingResultAggregator = wire[VotingResultAggregator]

}

trait CoreModule extends Macwire with DefaultShutdownHandlerModule
with ConfigModule with DBModule with ConfituraModule with VotesModule {

  lazy val actorSystem = ActorSystem("vc-main") onShutdown { actorSystem =>
    actorSystem.shutdown()
    actorSystem.awaitTermination()
  }

  lazy val conferenceDataInitializer = wire[ConferenceDataInitializer]
  lazy val testDataPopulator = wire[TestDataPopulator]
  lazy val flags = wire[AppFlags]

}

trait Beans extends CoreModule with ShutdownOnJVMTermination {
  lazy val webHandler = actorSystem.actorOf(Props(classOf[VoteCounterWebService], this), "vote-service")

  lazy val sslServer = new SslServer(webHandler, config, actorSystem)
}

object Beans extends Beans

