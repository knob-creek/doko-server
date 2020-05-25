package de.knobcreek.doko

data class Spieler(val nummer: Int) {
    var turnierStatus: TurnierStatus = TurnierStatus()
}

class SpielStatus(var partei: Partei) {
    var gespielteKarten = 0
    val ansagen = ArrayList<Ansage>()
    val stiche = ArrayList<Stich>()
}

class TurnierStatus
