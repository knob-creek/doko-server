package de.knobcreek.doko.spieler_spi.util

import de.knobcreek.doko.spieler_spi.Karte
import de.knobcreek.doko.spieler_spi.SpielSnapshot
import de.knobcreek.doko.spieler_spi.Stich
import de.knobcreek.doko.spieler_spi.Wert

/**
 * @author arno
 */

fun Spielregel.hatTrumpf() = this.farben().any { f -> f.trumpf }
fun Spielregel.bedienendeKarten(farbe: SpielFarbe, karten: Iterable<Karte>): Iterable<Karte> =
        karten.filter { k -> farbe(k) == farbe }
fun Spielregel.isZweiteKarteHöher(erste: Karte, zweite: Karte): Boolean =
    if(farbe(erste) == farbe(zweite)) {
        isZweiteKarteHöherRaw(erste, zweite)
    }
    else {
        farbe(zweite).trumpf
    }

fun Spielregel.höchsteKarte(karten: List<Karte>): Karte? = when(karten.size) {
    0 -> null
    else -> {
        var candidate = karten.get(0)

        for (karte in karten) {
            if (isZweiteKarteHöher(candidate, karte)) {
                candidate = karte
            }
        }

        candidate
    }
}


fun SpielSnapshot.spielregel(): Spielregel = TODO()

//TODO Polymorpher Zugriff auf Stich und GespielterStich?

// nur für Stiche, bei denen schon eine Karte gespielt ist
fun SpielSnapshot.farbe(stich: Stich) = spielregel().farbe(stich.karten[0])

fun SpielSnapshot.alleKarten(): List<Karte> = TODO()

fun SpielSnapshot.alleGespieltenKarten(): List<Karte> {
    val result = ArrayList<Karte>()
    for (stich in fertigeStiche) {
        result.addAll(stich.karten)
    }
    return result
}

fun punkte(karte: Karte): Int = when(karte.wert) {
    Wert.Neun -> 0
    Wert.Zehn -> 10
    Wert.Bube -> 2
    Wert.Dame -> 3
    Wert.König -> 4
    Wert.As -> 11
}

fun punkte(karten: Iterable<Karte>): Int =
        karten.fold(0, { acc, k -> acc + punkte(k) } )




