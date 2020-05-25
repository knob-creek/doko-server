package de.knobcreek.doko

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class KartenTest {

    enum class Parameter(val karte: Karte, val bedient: Karte, val erwartet: Boolean) {
        HERZ_BEDIENT_HERZ(herzKönig, herzAs, true),
        BUBE_BEDIENT_NICHT(herzBube, herzAs, false),
        HERZ10_BEDIENT_NICHT(herzZehn, herzAs, false),
        FUCHS_BEDIENT_TRUMPF(fuchs, herzBube, true),
        HERZ10_BEDIENT_TRUMPF(herzZehn, herzBube, true),
        HERZ_BEDIENT_NICHT(herzAs, herzBube, false)
    }

    @ParameterizedTest
    @EnumSource(Parameter::class)
    fun bedient(parameter: Parameter) {
        assertEquals(parameter.erwartet, parameter.karte.bedient(parameter.bedient))
    }
}

val herzBube = Karte(Farbe.HERZ, Wert.BUBE, true, 42, false)
val herzKönig = Karte(Farbe.HERZ, Wert.KÖNIG, false, 0, false)
val herzAs = Karte(Farbe.HERZ, Wert.AS, false, 0, false)
val fuchs = Karte(Farbe.KARO, Wert.AS, true, 42, false)
