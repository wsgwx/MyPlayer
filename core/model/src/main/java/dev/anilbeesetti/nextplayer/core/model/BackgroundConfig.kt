package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** What is drawn behind the whole app. */
@Serializable(with = BackgroundTypeSerializer::class)
enum class BackgroundType {
    /** Follows the theme surface colour. */
    DEFAULT,

    /** A user-picked picture, shown as-is with cards floating translucently above it. */
    IMAGE,
}

/** Tolerates values persisted by older builds (e.g. the removed COLOR type). */
object BackgroundTypeSerializer : KSerializer<BackgroundType> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "BackgroundType",
        kind = PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): BackgroundType =
        runCatching { BackgroundType.valueOf(decoder.decodeString()) }
            .getOrDefault(BackgroundType.DEFAULT)

    override fun serialize(encoder: Encoder, value: BackgroundType) {
        encoder.encodeString(value.name)
    }
}

/** Background of the app. [imageUri] is a persisted document URI. */
@Serializable
data class BackgroundConfig(
    val type: BackgroundType = BackgroundType.DEFAULT,
    val imageUri: String? = null,
)
