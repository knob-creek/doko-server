package de.knobcreek.doko

import java.util.Random
import java.util.UUID

enum class Ansage(val erlaubtBis : Int, val grenze: Int, val punkte: Int = 1) {
    RE(1, 120, 2),
    KONTRA(1, 120, 2),
    KEINE9(2, 150),
    KEINE6(3, 180),
    KEINE3(4, 210),
    SCHWARZ(5, 240)
}

class Spiel(val spieler: List<Spieler>) {
    val uuid: UUID = UUID.randomUUID()
    val random = Random()
    // einfacher Satz, wird nur zum Geben verwendet
    val alleKarten = erzeugeKarten(Spielregel.REGULÄR, true, false)
    val kartenJeSpieler = geben()
    val spielStatus = statusNachGeben()
    val vorbehalte = HashMap<Spieler, Spielregel>()

    var soloSpieler : Spieler? = null
    var spielregel = Spielregel.REGULÄR
    var aktuellerStich : Stich? = null
    val stiche = ArrayList<Stich>()

    fun geben(): Map<Spieler, MutableList<Karte>> {
        val gemischt = alleKarten.plus(alleKarten).shuffled(random)
        val proSpieler = gemischt.size / spieler.size
        return spieler
                .associateWith { sp ->
                    val fromIndex = sp.nummer * proSpieler
                    gemischt.subList(fromIndex, fromIndex + proSpieler).toMutableList()
                }
    }

    fun statusNachGeben(): Map<Spieler, SpielStatus> {
        return kartenJeSpieler.entries
                .associateBy({ entry -> entry.key}, { entry -> SpielStatus(partei(entry.value)) })
    }

    fun partei(karten: List<Karte>) =
            if (karten.contains(reDame)) Partei.RE else Partei.KONTRA

    fun vorbehalt(spielregel: Spielregel, spieler: Spieler) : Pair<Spieler, Spielregel>? {
        if (vorbehalte.containsKey(spieler))
            throw NichtErlaubtException("nur ein Vorbehalt")

        vorbehalte[spieler] = spielregel
        if (vorbehalte.size == 4) {
            with (vorbehalte.entries
                    .maxBy { entry -> entry.value }!!) {
                this@Spiel.spielregel = value
                if (value !in listOf(Spielregel.REGULÄR, Spielregel.HOCHZEIT)) {
                    soloSpieler = key
                    if (value != Spielregel.KARO)
                        kartenNeuBewerten(value)
                }
                return Pair(key, value)
            }
        }

        return null
    }

    fun kartenNeuBewerten(spielregel: Spielregel) {
        kartenJeSpieler.values
                .forEach { karten ->
                    karten.replaceAll {
                        karte -> Karte(karte.farbe, karte.wert, spielregel, true)
                    }
                }
    }

    fun spielen(karte: Karte, spieler: Spieler) {
        val karten = kartenJeSpieler[spieler] ?: throw NichtErlaubtException("ungültiger Spieler")

        val stich = aktuellerStich
        if (stich == null)
            aktuellerStich = Stich(karte, spieler)
        else {
            if (!stich.darfSpielen(spieler))
                throw NichtErlaubtException("nicht an der Reihe")
            if (!stich.darfSpielen(karte, karten))
                throw NichtErlaubtException("ungültige Karte")
            val aktuellerStich = stich.nächsteKarte(karte, spieler)
            if (aktuellerStich.gespielt()) {
                stiche.add(aktuellerStich)
                this.aktuellerStich = null
            } else
                this.aktuellerStich = aktuellerStich
        }
        karten.remove(karte)
        spielStatus[spieler]!!.gespielteKarten++
    }

    fun ansagen(ansage: Ansage, spieler: Spieler) {
        if ((spielStatus[spieler]?.gespielteKarten ?: 0) > ansage.erlaubtBis)
            throw NichtErlaubtException("$ansage nur erlaubt bis zur ${ansage.erlaubtBis}. Karte")
        spielStatus[spieler]?.ansagen?.add(ansage)
    }
}

class NichtErlaubtException(val grund: String) : Throwable(grund)
