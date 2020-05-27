package de.knobcreek.doko.spielerspi

/**
 * @author arno
 */
interface SpielerSpi {
    fun id(): String

    //TODO Pflichtsoli, Turnierstand, Rundennummer etc.
    /**
     * Jeder Spieler wird vor Beginn des eigentlichen Spiels gefragt, ob bzw. welchen Vorbehalt er anmeldet. Die
     *  anderen Spieler sehen später nur den einen Vorbehalt, der die höchste Priorität hat
     */
    fun vorbehalt(variante: RegelVariante, werBinIch: Spieler, ersterAufspieler: Spieler, hand: List<Karte>): Vorbehalt?

    /**
     * Liefert die Aktion eines Spielers auf Basis des aktuellen Spielstandes. Jeder Spieler wird über jede Aktion
     *  jedes anderen Spielers benachrichtigt, und jeder Spieler muss jeweils eine SpielerAktion zurückgeben
     *  ('Ack', wenn er nicht am Zug ist).
     */
    fun aktion(snapshot: SpielSnapshot): SpielerAktion
}



data class RegelVariante(
        val zweiteStichtErste: Boolean,
        val mitNeunen: Boolean,
        val soli: List<SoloTyp>
)

enum class Farbe {
    Kreuz, Pik, Herz, Karo
}
enum class Wert {
    Neun, Zehn, Bube, Dame, König, As
}
data class Karte(val farbe: Farbe, val wert: Wert)

enum class Spieler(val idx: Int) {
    Eins(0), Zwei(1), Drei(2), Vier(3);

    fun nächster() = Spieler.values()[(idx+1)%4];
}

sealed class Stich {
    abstract val aufspiel: Spieler
    abstract val karten: List<Karte>
}
data class OffenerStich(
        override val aufspiel: Spieler,
        override val karten: List<Karte>
): Stich()
data class FertigerStich(
        override val aufspiel: Spieler,
        override val karten: List<Karte>,
        val gewinner: Spieler
): Stich()

enum class SoloTyp {
    Karo, Herz, Pik, Kreuz,
    Fleischlos,
    Buben, Damen
}
enum class HochzeitTyp {
    ErsterFremder,
    ErsterFremderTrumpf
}

sealed class HochzeitKlärung
object HochzeitAllein: HochzeitKlärung()
data class HochzeitMit(val spieler: Spieler): HochzeitKlärung()

sealed class Vorbehalt
data class Solo(val typ: SoloTyp): Vorbehalt()
// Aus Sicht des Spielers passt es durchaus, die Klärung der Hochzeit (sofern erfolgt) in den Vorbehalt aufzunehmen
data class Hochzeit(val typ: HochzeitTyp, val klärung: HochzeitKlärung?): Vorbehalt()

enum class AnsageHöhe {
    Sieg, Keine90, Keine60, Keine30, Schwarz
}

sealed class SpielerAktion
data class KarteGespielt(val karte: Karte): SpielerAktion()
data class Re(val höhe: AnsageHöhe): SpielerAktion()
data class Contra(val höhe: AnsageHöhe): SpielerAktion()
object Ack: SpielerAktion() // als explizite Bestätigung, um "höflich" zu spielen - Spieler geben sich gegenseitig Zeit für Ansagen


data class SpielSnapshot(
        val variante: RegelVariante,

        // Information für den Spieler, wer "er selbst" ist
        val werBinIch: Spieler,
        val hand: List<Karte>,

        // nur ein Vorbehalt kann "greifen", die anderen bleiben unsichtbar
        val vorbehalt: Pair<Spieler, Vorbehalt>?,

        val fertigeStiche: List<FertigerStich>,
        val aktuellerStich: OffenerStich,

        val journal: List<Pair<Spieler, SpielerAktion>>
)