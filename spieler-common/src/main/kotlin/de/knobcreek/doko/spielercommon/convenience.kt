package de.knobcreek.doko.spielercommon

import de.knobcreek.doko.spielerspi.*

/**
 * @author arno
 */

val KreuzDame = Karte(Farbe.Kreuz, Wert.Dame)


fun Spielregel.hatTrumpf() = this.farben().any { f -> f.trumpf }
fun Spielregel.bedienendeKarten(farbe: SpielFarbe, karten: Iterable<Karte>): KartenSet =
        KartenSet(karten.filter { k -> farbe(k) == farbe })
fun Spielregel.isZweiteKarteHöher(erste: Karte, zweite: Karte): Boolean =
    if(farbe(erste) == farbe(zweite)) {
        isZweiteKarteHöherRaw(erste, zweite)
    }
    else {
        farbe(zweite).trumpf
    }

fun Spielregel.höchsteKarte(karten: KartenSet): Karte? = when(karten.size()) {
    0 -> null
    else -> {
        var candidate = karten.iterator().next()

        for (karte in karten) {
            if (isZweiteKarteHöher(candidate, karte)) {
                candidate = karte
            }
        }

        candidate
    }
}


fun FertigerStich.karteVon(spieler: Spieler): Karte = karten[(spieler.idx - aufspiel.idx + 4) % 4]

fun Stich.hatNichtBedient(spieler: Spieler, spielregel: Spielregel): Boolean =
        //NB: Für offene Stiche ist das *nicht* das Gleiche wie !Stich.hatBedient()!
        if (this is FertigerStich) {
            spielregel.farbe(karteVon(spieler)) != spielregel.farbe(karten[0])
        }
        else {
            val idx = (spieler.idx - aufspiel.idx + 4) % 4
            if (idx < karten.size) {
                spielregel.farbe(karten[idx]) != spielregel.farbe(karten[0])
            }
            else {
                false
            }
        }


fun SpielSnapshot.spielregel(): Spielregel = TODO()

fun SpielSnapshot.meineHand() = KartenSet(hand)

fun SpielSnapshot.farbe(stich: Stich): SpielFarbe? =
        if (stich.karten.isNotEmpty()) {
            spielregel().farbe(stich.karten[0])
        }
        else {
            null
        }

fun SpielSnapshot.alleKarten(): KartenSet = TODO()

fun SpielSnapshot.alleGespieltenKarten(): KartenSet =
        fertigeStiche
                .fold(KartenSet.empty, {acc, stich -> acc + stich.karten}) +
                aktuellerStich.karten

fun SpielSnapshot.alleUngespieltenKarten(): KartenSet  = alleKarten() - alleUngespieltenKarten()

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




