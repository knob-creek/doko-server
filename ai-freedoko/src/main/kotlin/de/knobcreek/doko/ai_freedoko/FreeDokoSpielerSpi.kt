package de.knobcreek.doko.ai_freedoko

import de.knobcreek.doko.spieler_spi.*

/**
 * @author arno
 */
class FreeDokoSpielerSpi: SpielerSpi {
    override fun id() = "free-doko-ai"

    override fun vorbehalt(variante: RegelVariante, werBinIch: Spieler, ersterAufspieler: Spieler, hand: List<Karte>): Vorbehalt? {
        TODO("Not yet implemented")
    }

    override fun aktion(snapshot: SpielSnapshot): SpielerAktion {
        TODO("Not yet implemented")
    }
}