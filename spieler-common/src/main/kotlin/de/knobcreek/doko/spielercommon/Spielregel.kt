package de.knobcreek.doko.spielercommon

import de.knobcreek.doko.spielerspi.*

/**
 * Eine Spielregel legt die Rangfolge von Karten fest. Das umfasst auch die Unterscheidung, ob es Trumpf gibt
 *  und welche Karten Trumpf sind.
 *
 * @author arno
 */
interface Spielregel {
    fun farben(): Set<SpielFarbe>

    /**
     * Ergbebnis muss nur innerhalb einer SpielFarbe definiert sein
     */
    fun isZweiteKarteHöherRaw(erste: Karte, zweite: Karte): Boolean
    fun farbe(karte: Karte): SpielFarbe

    companion object Factory {
        fun create(variante: RegelVariante, vorbehalt: Vorbehalt?): Spielregel {
            TODO()
        }
    }
}


/**
 * Eine SpielFarbe kategorisiert Karten danach, welche Karten welche Karten bedienen können. Die "Farbe eines Stichs"
 *  ist z.B. eine SpielFarbe und keine Farbe. Je nach Spielregel gibt es im Allgemeinen nicht alle SpielFarben
 *  (z.B. entweder Karo oder Trumpf).
 */
enum class SpielFarbe(val trumpf: Boolean) {
    Kreuz(false), Pik(false), Herz(false), Karo(false),
    Trumpf(true)
}
