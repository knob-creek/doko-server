package de.knobcreek.doko

class Stich(val nummer: Int,
            val aufgespielt: Karte,
            val letzterSpieler: Spieler,
            val karten: List<GespielteKarte>) {
    constructor(nummer: Int, aufgespielt: Karte, spieler: Spieler) :
            this(nummer, aufgespielt, spieler, listOf(GespielteKarte(0, aufgespielt, spieler)))

    fun nächsteKarte(karte: Karte, spieler: Spieler) =
        Stich(nummer, aufgespielt, spieler, karten.plus(GespielteKarte(karten.size, karte, spieler)))

    fun darfSpielen(spieler: Spieler) =
            spieler.nummer == (letzterSpieler.nummer + 1) % 4

    fun darfSpielen(karte: Karte, hatKarten: List<Karte>) =
            karte in hatKarten &&
            !hatKarten.any { kt -> kt.bedient(aufgespielt) } || karte.bedient(aufgespielt)

    fun gespielt() =
            karten.size == 4

    fun hatGewonnen() : Spieler {
        return karten
                .maxBy { gespielteKarte -> with(gespielteKarte.karte) {
                    when {
                        aufgespielt.trumpf || trumpf -> trumpfHöhe
                        farbe == aufgespielt.farbe -> wert.ordinal
                        else -> 0
                    } * 10 + (if (trumpf && herzZehn) gespielteKarte.nummer else -gespielteKarte.nummer)
                }}
                ?.spieler!!
    }

    fun punkte() =
            karten.map { gk -> gk.karte.wert.punkte }
                    .sum()
}

data class GespielteKarte(val nummer: Int, val karte: Karte, val spieler: Spieler)
