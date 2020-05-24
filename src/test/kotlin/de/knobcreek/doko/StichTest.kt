package de.knobcreek.doko

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

internal class StichTest {
    val spieler = (0..3)
            .map { n -> Spieler(n) }

    val pik = listOf(Wert.KÖNIG, Wert.ZEHN, Wert.AS)
            .map { wert -> Karte(Farbe.PIK, wert, Spielregel.KARO, true) }

    @Test
    fun darfSpielen() {
        val stich = Stich(0, pikAs, spieler[0])
        assertSame(pikAs, stich.aufgespielt)
        assertEquals(1, stich.karten.size)

        assertTrue(stich.darfSpielen(spieler[1]))
        assertFalse(stich.darfSpielen(spieler[2]))
        assertFalse(stich.darfSpielen(spieler[3]))
        assertTrue(stich.darfSpielen(pikAs, pik))
    }

    @Test
    fun darfNichtSpielen() {
        val stich = Stich(0, pikAs, spieler[3])

        assertTrue(stich.darfSpielen(spieler[0]))
        assertFalse(stich.darfSpielen(spieler[1]))
        assertFalse(stich.darfSpielen(spieler[2]))

        // hat er gar nicht
        assertFalse(stich.darfSpielen(karoNeun, pik))
        // darf er trotzdem nicht
        assertFalse(stich.darfSpielen(karoNeun, pik.plus(karoNeun)))
    }

    enum class Parameter(val aufspiel: Karte) {
        KARO_NEUN(karoNeun),
        PIK_AS(pikAs)
    }

    @ParameterizedTest
    @EnumSource(Parameter::class)
    fun `wird aufgespielt und sticht`(parameter: Parameter) {
        val aufspiel = Stich(0, parameter.aufspiel, spieler[0])

        var stich = aufspiel
        for (n in 0..2)
            stich = stich.nächsteKarte(pik[n], spieler[n + 1])

        assertEquals(4, stich.karten.size)
        assertSame(spieler[0], stich.hatGewonnen())
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun `Pik wird aufgespielt, Karo 9 sticht`(karoSticht: Int) {

        val aufspiel = Stich(0, pik[0], spieler[0])
        var stich = aufspiel

        for (n in 1 until karoSticht)
            stich = stich.nächsteKarte(pik[n], spieler[n])

        stich = stich.nächsteKarte(karoNeun, spieler[karoSticht])

        for (n in karoSticht..2)
            stich = stich.nächsteKarte(pik[n], spieler[n + 1])

        assertEquals(4, stich.karten.size)
        assertSame(spieler[karoSticht], stich.hatGewonnen())
    }

    @Test
    fun `fleischlos sticht die erste Herz 10`() {
        val karten = listOf(Wert.BUBE, Wert.ZEHN, Wert.DAME, Wert.ZEHN)
                .map { wert -> Karte(Farbe.HERZ, wert, Spielregel.FLEISCHLOS, true) }

        val gespielteKarten = karten.indices
                .map { n -> GespielteKarte(n, karten[n], spieler[n]) }

        val stich = Stich(0, karten[0], spieler[3], gespielteKarten)

        assertSame(spieler[1], stich.hatGewonnen())
    }

    fun zweiteStichtImmer(karten: List<Karte>) {
        val gespielteKarten = karten.indices
                .map { n -> GespielteKarte(n, karten[n], spieler[n]) }
        val aufspiel = Stich(0, karten[0], spieler[2], gespielteKarten)

        val stich = aufspiel.nächsteKarte(herzZehn, spieler[3])

        assertEquals(4, stich.karten.size)
        assertSame(spieler[3], stich.hatGewonnen())
    }

    @TestFactory
    fun `zweite Herz 10 sticht immer`() : List<DynamicTest> {
        val alleKarten = erzeugeKarten(Spielregel.KARO, true, true)

        return alleKarten
                .flatMap { karte1 -> alleKarten
                        // nur eine Herz-10
                        .filter { karte2 -> karte1 != herzZehn || karte2 != herzZehn }
                        .flatMap { karte2 -> alleKarten
                                .filter { karte3 ->
                                    if (karte1 == herzZehn || karte2 == herzZehn)
                                        karte3 != herzZehn
                                    else
                                        karte3 != karte1 || karte3 != karte2
                                }
                                .map { karte3 -> DynamicTest.dynamicTest("$karte1-$karte2-$karte3") {
                                    zweiteStichtImmer(listOf(karte1, karte2, karte3))
                                }}
                        }
                }
    }
}

val karoNeun = Karte(Farbe.KARO, Wert.NEUN, Spielregel.KARO, true)
val herzZehn = Karte(Farbe.HERZ, Wert.ZEHN, Spielregel.KARO, true)
val pikAs = Karte(Farbe.PIK, Wert.AS, Spielregel.KARO, true)
