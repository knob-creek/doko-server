package de.knobcreek.doko

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class SpielTest {
    val spieler = (0..3)
            .map { n -> Spieler(n) }

    @Test
    fun geben() {
        val spiel = Spiel(spieler)
        assertEquals(20, spiel.alleKarten.size)
        assertEquals(4, spiel.kartenJeSpieler.size)
        assertAll(
                spiel.kartenJeSpieler.values
                        .map { karten -> Executable { assertEquals(10, karten.size) } }
        )
    }

    @Test
    fun keinVorbehalt() {
        val spiel = Spiel(spieler)
        assertAll(
                spieler.subList(0, 3)
                        .map { sp -> Executable { assertNull(spiel.vorbehalt(Spielregel.REGULÄR, sp)) }}
        )
        val regeln = spiel.vorbehalt(Spielregel.REGULÄR, spieler[3])
        assertNotNull(regeln)
        assertSame(Spielregel.REGULÄR, regeln?.second)
        assertNull(spiel.soloSpieler)
    }

    enum class Parameter(val spielregel: Spielregel, val trumpf: (Karte) -> Boolean) {
        KARO(Spielregel.KARO, { karte -> karte.wert in listOf(Wert.BUBE, Wert.DAME) || karte.farbe == Farbe.KARO || karte == herzZehn }),
        HERZ(Spielregel.HERZ, { karte -> karte.wert in listOf(Wert.BUBE, Wert.DAME) || karte.farbe == Farbe.HERZ }),
        PIK(Spielregel.PIK, { karte -> karte.wert in listOf(Wert.BUBE, Wert.DAME) || karte.farbe == Farbe.PIK || karte == herzZehn }),
        KREUZ(Spielregel.KREUZ, { karte -> karte.wert in listOf(Wert.BUBE, Wert.DAME) || karte.farbe == Farbe.KREUZ || karte == herzZehn }),
        BUBEN(Spielregel.BUBEN, { karte -> karte.wert == Wert.BUBE }),
        DAMEN(Spielregel.DAMEN, { karte -> karte.wert == Wert.DAME })
    }

    @ParameterizedTest
    @EnumSource(Parameter::class)
    fun kartenNeuBewerten(parameter: Parameter) {
        val spiel = Spiel(spieler)
        spiel.kartenNeuBewerten(parameter.spielregel)

        assertAll(
                spiel.kartenJeSpieler.values
                        .flatten()
                        .map { karte -> Executable { assertEquals(parameter.trumpf(karte), karte.trumpf) } }
        )
    }

    @Test
    fun fleischlos() {
        val spiel = Spiel(spieler)
        spieler.subList(0, 3)
                .forEach { sp -> spiel.vorbehalt(Spielregel.REGULÄR, sp) }
        val regeln = spiel.vorbehalt(Spielregel.FLEISCHLOS, spieler[3])
        assertNotNull(regeln)
        assertSame(spieler[3], regeln?.first)
        assertSame(Spielregel.FLEISCHLOS, regeln?.second)
        assertSame(spieler[3], spiel.soloSpieler)

        assertAll(
                spiel.kartenJeSpieler.values
                        .flatten()
                        .map { karte -> Executable { assertFalse(karte.trumpf) } }
        )
    }

    @Test
    fun damenSolo() {
        val spiel = Spiel(spieler)
        spiel.vorbehalt(Spielregel.REGULÄR, spieler[0])
        spiel.vorbehalt(Spielregel.BUBEN, spieler[1])
        spiel.vorbehalt(Spielregel.DAMEN, spieler[2])
        val regeln = spiel.vorbehalt(Spielregel.FLEISCHLOS, spieler[3])
        assertNotNull(regeln)

        assertSame(spieler[2], regeln?.first)
        assertSame(Spielregel.DAMEN, regeln?.second)
        assertSame(spieler[2], spiel.soloSpieler)

        assertAll(
                spiel.kartenJeSpieler.values
                        .flatten()
                        .map { karte -> Executable { assertEquals(karte.wert == Wert.DAME, karte.trumpf) } }
        )
    }

    @Test
    fun stichSpielen() {
        val spiel = Spiel(spieler)

        val spieler = spiel.spieler
        spieler.forEach { sp -> spiel.vorbehalt(Spielregel.REGULÄR, sp) }
        assertNull(spiel.soloSpieler)

        with(spieler[0]) {
            val karten = spiel.kartenJeSpieler[this]
            assertEquals(10, karten!!.size)
            spiel.spielen(karten.first(), this)
            assertEquals(9, karten.size)
            assertNotNull(spiel.aktuellerStich)
            assertThrows(NichtErlaubtException::class.java, { spiel.spielen(karten.first(), this) })
        }

        assertAll(spieler.subList(1, 4)
                .map { spieler123 -> Executable {
                    val karten = spiel.kartenJeSpieler[spieler123]
                    assertEquals(10, karten?.size)
                    val gespielt = karten?.asSequence()
                            ?.filter { karte -> try {
                                spiel.spielen(karte, spieler123)
                                true
                            } catch (nex: NichtErlaubtException) {
                                false
                            }}
                            ?.first()
                    assertNotNull(gespielt)
                    assertEquals(9, karten?.size)
                }}
        )

        assertNull(spiel.aktuellerStich)
        assertEquals(1, spiel.stiche.size)
    }
}
