package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Built-in colour presets. Each maps to a hand-tuned full light + dark scheme in
 * `core.ui.theme.Palettes` (Material You baseline style).
 */
@Serializable(with = ThemePaletteSerializer::class)
enum class ThemePalette {
    CINEMA,
    AMBER,
    TANGERINE,
    OCEAN,
    FOREST,
}

/** Tolerates palettes persisted by older builds. */
object ThemePaletteSerializer : KSerializer<ThemePalette> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "ThemePalette",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): ThemePalette =
        runCatching { ThemePalette.valueOf(decoder.decodeString()) }
            .getOrDefault(ThemePalette.CINEMA)

    override fun serialize(encoder: Encoder, value: ThemePalette) {
        encoder.encodeString(value.name)
    }
}
