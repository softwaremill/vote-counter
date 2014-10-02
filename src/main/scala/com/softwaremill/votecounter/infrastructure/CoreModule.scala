package com.softwaremill.votecounter.infrastructure

import akka.actor.{ActorSystem, Props}
import com.softwaremill.macwire.Macwire
import com.softwaremill.thegarden.lawn.shutdownables._
import com.softwaremill.votecounter.common._
import com.softwaremill.votecounter.config.VoteCounterConfig
import com.softwaremill.votecounter.confitura._
import com.softwaremill.votecounter.db._
import com.softwaremill.votecounter.h2.{ConferenceDataInitializer, DBInitializer, SQLDatabase, TestDataPopulator}
import com.softwaremill.votecounter.jdd.{JddAgendaReader, JddDevices, JddRooms, JddTalks}
import com.softwaremill.votecounter.voting.{ResultsToCsvTransformer, VoteCountsAggregator, VoteRequestProcessor, VotingResultAggregator}
import com.softwaremill.votecounter.web.{SslServer, VoteCounterWebService}
import com.typesafe.config.ConfigFactory


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
  lazy val agendaFileReader = wire[ConfituraAgendaReader]

  lazy val roomsProvider: RoomsProvider = wire[ConfituraRooms]
  lazy val talksProvider: TalksProvider = new ConfituraTalks(agendaFileReader,
    config.conferenceDate, config.conferenceTimeZone)
  lazy val devicesProvider: DevicesProvider = wire[ConfituraDevices]
}

trait JddModule extends Macwire with ConfigModule {
  lazy val jddTalksReader = wire[JddAgendaReader]

  lazy val roomsProvider: RoomsProvider = wire[JddRooms]
  lazy val talksProvider: TalksProvider = new JddTalks(jddTalksReader)
  lazy val devicesProvider: DevicesProvider = wire[JddDevices]
}

trait VotesModule extends Macwire with DBModule {

  lazy val votesRequestProcessor = wire[VoteRequestProcessor]

  lazy val votingResultAggregator = wire[VotingResultAggregator]

  lazy val voteCountsAggregator = wire[VoteCountsAggregator]

  lazy val resultsToCsvTransformer = wire[ResultsToCsvTransformer]
}

trait CoreModule extends Macwire with DefaultShutdownHandlerModule
with ConfigModule with DBModule with JddModule with VotesModule {

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

