package com.modules.core.datastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.modules.core.datastore.models.PlayerPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer for PlayerPreferences using JSON format
 */
object PlayerPreferencesSerializer : Serializer<PlayerPreferences> {

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    /**
     * Default value when no data exists
     */
    override val defaultValue: PlayerPreferences
        get() = PlayerPreferences()

    /**
     * Reads PlayerPreferences from input stream
     * @throws CorruptionException if deserialization fails
     */
    override suspend fun readFrom(input: InputStream): PlayerPreferences {
        try {
            return jsonFormat.decodeFromString(
                deserializer = PlayerPreferences.serializer(),
                string = input.readBytes().decodeToString(),
            )
        } catch (exception: SerializationException) {
            throw CorruptionException("Cannot read datastore", exception)
        }
    }

    /**
     * Writes PlayerPreferences to output stream
     * @param t PlayerPreferences to serialize
     * @param output Target output stream
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: PlayerPreferences, output: OutputStream) {
        output.write(
            jsonFormat.encodeToString(
                serializer = PlayerPreferences.serializer(),
                value = t,
            ).encodeToByteArray(),
        )
    }
}
