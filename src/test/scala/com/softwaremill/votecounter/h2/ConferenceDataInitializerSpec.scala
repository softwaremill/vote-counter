package com.softwaremill.votecounter.h2

import com.softwaremill.votecounter.common.{Agenda, AgendaProvider}
import com.softwaremill.votecounter.db.TalksDao
import com.softwaremill.votecounter.infrastructure.AppFlags
import org.mockito.Matchers.{anyObject, anyString}
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar

class ConferenceDataInitializerSpec extends FlatSpec with MockitoSugar {

  it should "replace talks if current agenda is outdated" in {
    // given
    val agendaProvider = mock[AgendaProvider]
    when(agendaProvider.read()).thenReturn(Agenda("2", List()))

    val agendaVersionAccessor = mock[AgendaVersionAccessor]
    when(agendaVersionAccessor.read()).thenReturn(Some("1"))

    val talksDao = mock[TalksDao]
    val appFlags = mock[AppFlags]

    val conferenceDataInitializer = new ConferenceDataInitializer(null, null, agendaProvider, talksDao, null, null, appFlags, agendaVersionAccessor)

    // when
    conferenceDataInitializer.updateTalksIfAgendaIsOutdated()

    // then
    verify(talksDao, times(1)).replaceAll(List())
    verify(agendaVersionAccessor, times(1)).write("2")
  }

  it should "not replace talks if conference data was initialized and current agenda is up to date" in {
    // given
    val agendaProvider = mock[AgendaProvider]
    when(agendaProvider.read()).thenReturn(Agenda("1", List()))

    val agendaVersionAccessor = mock[AgendaVersionAccessor]
    when(agendaVersionAccessor.read()).thenReturn(Some("1"))

    val appFlags = mock[AppFlags]
    when(appFlags.isConferenceDataInitialized).thenReturn(true)

    val talksDao = mock[TalksDao]

    val conferenceDataInitializer = new ConferenceDataInitializer(null, null, agendaProvider, talksDao, null, null, appFlags, agendaVersionAccessor)

    // when
    conferenceDataInitializer.updateTalksIfAgendaIsOutdated()

    // then
    verify(talksDao, never()).replaceAll(anyObject())
    verify(agendaVersionAccessor, never()).write(anyString())
  }

  it should "replace talks if no agenda version is present and conference data is not initialized" in {
    // given
    val agendaProvider = mock[AgendaProvider]
    when(agendaProvider.read()).thenReturn(Agenda("3", List()))

    val agendaVersionAccessor = mock[AgendaVersionAccessor]
    when(agendaVersionAccessor.read()).thenReturn(None)

    val appFlags = mock[AppFlags]
    when(appFlags.isConferenceDataInitialized).thenReturn(false)

    val talksDao = mock[TalksDao]

    val conferenceDataInitializer = new ConferenceDataInitializer(null, null, agendaProvider, talksDao, null, null, appFlags, agendaVersionAccessor)

    // when
    conferenceDataInitializer.updateTalksIfAgendaIsOutdated()

    // then
    verify(talksDao, times(1)).replaceAll(List())
    verify(agendaVersionAccessor, times(1)).write("3")
  }

  it should "not replace talks if no agenda version is present and conference data is initialized" in {
    // given
    val agendaProvider = mock[AgendaProvider]
    when(agendaProvider.read()).thenReturn(Agenda("2", List()))

    val agendaVersionAccessor = mock[AgendaVersionAccessor]
    when(agendaVersionAccessor.read()).thenReturn(None)

    val appFlags = mock[AppFlags]
    when(appFlags.isConferenceDataInitialized).thenReturn(true)

    val talksDao = mock[TalksDao]

    val conferenceDataInitializer = new ConferenceDataInitializer(null, null, agendaProvider, talksDao, null, null, appFlags, agendaVersionAccessor)

    // when
    conferenceDataInitializer.updateTalksIfAgendaIsOutdated()

    // then
    verify(talksDao, never()).replaceAll(anyObject())
    verify(agendaVersionAccessor, never()).write(anyString())
  }
}
