package de.knobcreek.doko

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

internal class SpielTest {
    val spieler = (0..3)
            .map { n -> Spieler(n) }

    enum class NeunenParameter(val mitNeuen: Boolean, val anzahl: Int) {
        MIT_NEUNEN(true, 12),
        OHNE_NEUNEN(false, 10)
    }

    @ParameterizedTest
    @EnumSource(NeunenParameter::class)
    fun geben(parameter: NeunenParameter) {
        val spiel = Spiel(spieler, parameter.mitNeuen, true)
        assertEquals(parameter.anzahl * 2, spiel.alleKarten.size)
        assertEquals(4, spiel.kartenJeSpieler.size)
        assertAll(
                spiel.kartenJeSpieler.values
                        .map { Executable { assertEquals(parameter.anzahl, it.size) } }
        )
    }

    @Test
    fun keinVorbehalt() {
        val spiel = Spiel(spieler, false, true)
        assertAll(
                spieler.subList(0, 3)
                        .map { Executable { assertNull(spiel.vorbehalt(Spielregel.REGULÄR, it)) }}
        )
        val regeln = spiel.vorbehalt(Spielregel.REGULÄR, spieler[3])
        assertNotNull(regeln)
        assertSame(Spielregel.REGULÄR, regeln?.second)
        assertNull(spiel.soloSpieler)
    }

    enum class SpielregelParameter(val spielregel: Spielregel, val trumpf: (Karte) -> Boolean) {
        KARO(Spielregel.KARO, { it.wert in listOf(Wert.BUBE, Wert.DAME) || it.farbe == Farbe.KARO || it == herzZehn }),
        HERZ(Spielregel.HERZ, { it.wert in listOf(Wert.BUBE, Wert.DAME) || it.farbe == Farbe.HERZ }),
        PIK(Spielregel.PIK, { it.wert in listOf(Wert.BUBE, Wert.DAME) || it.farbe == Farbe.PIK || it == herzZehn }),
        KREUZ(Spielregel.KREUZ, { it.wert in listOf(Wert.BUBE, Wert.DAME) || it.farbe == Farbe.KREUZ || it == herzZehn }),
        BUBEN(Spielregel.BUBEN, { it.wert == Wert.BUBE }),
        DAMEN(Spielregel.DAMEN, { it.wert == Wert.DAME })
    }

    @ParameterizedTest
    @EnumSource(SpielregelParameter::class)
    fun kartenNeuBewerten(parameter: SpielregelParameter) {
        val spiel = Spiel(spieler, true, true)
        val kartenBewertungen = HashMap<Karte, KartenBewertung>()
        spiel.kartenBewerten(kartenBewertungen, parameter.spielregel)

        assertAll(
                kartenBewertungen.values
                        .map { Executable { assertEquals(parameter.trumpf(it.karte), it.trumpf) } }
        )
    }

    @ParameterizedTest
    @ValueSource(ints = [ 0, 1, 2, 3 ])
    fun fleischlos(fleischlosSpieler: Int) {
        val spiel = Spiel(spieler, false, true)
        spieler.forEach {
            val spielregel = if (it.nummer == fleischlosSpieler)
                Spielregel.FLEISCHLOS
            else
                Spielregel.REGULÄR
            spiel.vorbehalt(spielregel, it)
        }
        with (spiel) {
            assertSame(Spielregel.FLEISCHLOS, spielregel)
            assertSame(spieler[fleischlosSpieler], soloSpieler)
            assertAll(
                    kartenBewertungen.values
                            .map { Executable { assertFalse(it.trumpf) } }
            )
        }
    }

    @Test
    fun damenSolo() {
        val spiel = Spiel(spieler, false, true)
        spiel.vorbehalt(Spielregel.REGULÄR, spieler[0])
        spiel.vorbehalt(Spielregel.BUBEN, spieler[1])
        spiel.vorbehalt(Spielregel.DAMEN, spieler[2])
        val regeln = spiel.vorbehalt(Spielregel.FLEISCHLOS, spieler[3])
        assertNotNull(regeln)

        assertSame(spieler[2], regeln?.first)
        assertSame(Spielregel.DAMEN, regeln?.second)
        assertSame(spieler[2], spiel.soloSpieler)

        assertAll(
                spiel.kartenBewertungen.values
                        .map { Executable { assertEquals(it.karte.wert == Wert.DAME, it.trumpf) } }
        )
    }

    @Test
    fun stichSpielen() {
        val spiel = Spiel(spieler, false, true)

        val spieler = spiel.spieler
        spieler.forEach { spiel.vorbehalt(Spielregel.REGULÄR, it) }
        assertNull(spiel.soloSpieler)

        with(spieler[0]) {
            val karten = spiel.kartenJeSpieler[this]
            assertEquals(10, karten!!.size)
            spiel.spielen(karten.first(), this)
            assertEquals(9, karten.size)
            assertNotNull(spiel.aktuellerStich)
            assertThrows(NichtErlaubtException::class.java) { spiel.spielen(karten.first(), this) }
        }

        assertAll(spieler.subList(1, 4)
                .map {
                    Executable {
                        val karten = spiel.kartenJeSpieler[it]
                        assertEquals(10, karten?.size)
                        val gespielt = karten?.asSequence()
                                ?.filter { karte -> try {
                                    spiel.spielen(karte, it)
                                    true
                                } catch (nex: NichtErlaubtException) {
                                    false
                                }}
                                ?.first()
                        assertNotNull(gespielt)
                        assertEquals(9, karten?.size)
                    }
                }
        )

        assertNull(spiel.aktuellerStich)
        assertEquals(1, spiel.stiche.size)
    }
}
