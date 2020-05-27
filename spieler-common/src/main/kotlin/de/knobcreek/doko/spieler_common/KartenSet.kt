package de.knobcreek.doko.spieler_common

import de.knobcreek.doko.spieler_spi.Karte

/**
 * @author arno
 */
class KartenSet(private val karten: List<Karte>): Iterable<Karte> {
    override fun iterator(): Iterator<Karte> = karten.iterator()

    operator fun plus(karte: Karte) = KartenSet(karten + karte)
    operator fun plus(karten: Iterable<Karte>) = KartenSet(this.karten + karten)

    operator fun minus(karte: Karte) = KartenSet(karten - karte)
    operator fun minus(karten: Iterable<Karte>): KartenSet {
        //NB '-' mit einer Collection als Argument hat hier andere Semantik
        val result = this.karten.toMutableList()
        for (karte in karten) {
            result -= karte
        }
        return KartenSet(result)
    }

    fun filter(pred: (Karte) -> Boolean) = KartenSet(karten.filter(pred))

    fun contains(karte: Karte) = karten.contains(karte)
    fun anzahl(karte: Karte) = karten.filter { k -> k == karte }.count()

    fun size() = karten.size

    override fun equals(other: Any?): Boolean {
        return if (other is KartenSet) {
            this.karten == other.karten
        }
        else {
            false
        }
    }

    companion object Factory {
        val empty = KartenSet(listOf())
    }
}