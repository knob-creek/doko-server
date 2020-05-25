package de.knobcreek.doko

enum class Partei(val gewonnen: (Int, Int) -> Boolean,
                  val regulärePunkte: (Int) -> Int) {
    // bei Hochzeiten sind alle Spieler ohne Kreuz Dame erst einmal Kontra
    RE({ punkte, grenze -> punkte > grenze }, { punkte -> punkte / 240 + (punkte - 91) / 30 }),
    KONTRA({ punkte, grenze -> punkte >= grenze }, { punkte -> (punkte - 90) / 30 })
}
