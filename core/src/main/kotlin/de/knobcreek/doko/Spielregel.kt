package de.knobcreek.doko

/**
 * Interessanterweise sind die Spielregeln unabhängig davon, ob ein Solo
 * gespielt wird oder nicht.  Die "Standard-Spielregel" ist KARO.
 */
enum class Spielregel(val istTrumpf: (farbe: Farbe, wert: Wert) -> Boolean,
                      val trumpfHöhe: (farbe: Farbe, wert: Wert) -> Int) {
    REGULÄR(Farbe.KARO),
    HOCHZEIT(Farbe.KARO),
    FLEISCHLOS({ _, _ -> false }, { _, _ -> 0 }),
    KARO(Farbe.KARO),
    HERZ(Farbe.HERZ),
    PIK(Farbe.PIK),
    KREUZ(Farbe.KREUZ),
    BUBEN(Wert.BUBE),
    DAMEN(Wert.DAME);

    /**
     * Die normalen Spielregeln oder ein Farbsolo
     */
    constructor(trumpfFarbe: Farbe) :
            this({ farbe, wert -> istTrumpf(trumpfFarbe, farbe, wert) },
                { farbe, wert -> trumpfHöhe(trumpfFarbe, farbe, wert) })

    /**
     * Damen- oder Bubensolo
     */
    constructor(trumpf: Wert) :
            this({ _, wert -> wert == trumpf },
                { farbe, _ -> farbe.ordinal + 10 })
}

fun istTrumpf(trumpfFarbe: Farbe, farbe: Farbe, wert: Wert): Boolean =
    farbe == trumpfFarbe || wert == Wert.BUBE || wert == Wert.DAME || herzZehn(farbe, wert)

/**
 * Die Trumpfhöhe ist immer höher als der Wert einer Fehlfarbe.
 */
fun trumpfHöhe(trumpfFarbe: Farbe, farbe: Farbe, wert: Wert) =
    when {
        herzZehn(farbe, wert) -> 99
        wert == Wert.DAME -> 30 + farbe.ordinal
        wert == Wert.BUBE -> 20 + farbe.ordinal
        farbe == trumpfFarbe -> 10 + wert.ordinal
        else -> 0
    }
