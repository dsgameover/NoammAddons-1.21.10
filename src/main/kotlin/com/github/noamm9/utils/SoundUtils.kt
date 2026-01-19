package com.github.noamm9.utils

import com.github.noamm9.NoammAddons
import com.github.noamm9.NoammAddons.mc
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.JOrbisAudioStream
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import org.lwjgl.openal.AL10
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread

object SoundUtils {
    private val SOUNDS_DIR = Paths.get("config", NoammAddons.MOD_NAME, "sounds")

    fun playEvent(event: Holder.Reference<SoundEvent>, volume: Float = 1.0f, pitch: Float = 1f) {
        playEvent(event.value(), volume, pitch)
    }

    fun playEvent(event: SoundEvent, volume: Float = 1.0f, pitch: Float = 1f) {
        val weightedSoundSet = mc.soundManager.getSoundEvent(event.location) ?: return
        val sound = weightedSoundSet.getSound(SoundInstance.createUnseededRandom())
        val soundLocation = sound.location
        val actualPath = "sounds/${soundLocation.path}.ogg"
        val fileLocation = ResourceLocation.fromNamespaceAndPath(soundLocation.namespace, actualPath)
        playInternal(fileLocation, volume, pitch)
    }

    fun playExternal(fileName: String, volume: Float = 1.0f, pitch: Float = 1f) {
        val soundPath = SOUNDS_DIR.resolve(fileName)
        if (! Files.exists(soundPath)) return

        thread(start = true) {
            runCatching {
                playRawStream(Files.newInputStream(soundPath), volume, pitch)
            }.onFailure { it.printStackTrace() }
        }
    }

    fun playInternal(id: ResourceLocation, volume: Float = 1.0f, pitch: Float = 1f) {
        val resource = mc.resourceManager.getResource(id)
        if (resource.isEmpty) return

        thread(start = true) {
            runCatching {
                playRawStream(resource.get().open(), volume, pitch)
            }.onFailure { it.printStackTrace() }
        }
    }

    private fun playRawStream(inputStream: InputStream, volume: Float = 1f, pitch: Float = 1f) {
        JOrbisAudioStream(inputStream).use { ogg ->
            val audioData = ogg.readAll()
            val format = if (ogg.format.channels == 1) AL10.AL_FORMAT_MONO16 else AL10.AL_FORMAT_STEREO16
            val sampleRate = ogg.format.sampleRate.toInt()

            val bufferId = AL10.alGenBuffers()
            AL10.alBufferData(bufferId, format, audioData, sampleRate)

            val sourceId = AL10.alGenSources()
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId)

            AL10.alSourcef(sourceId, AL10.AL_GAIN, volume)
            AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch)

            AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE)
            AL10.alSource3f(sourceId, AL10.AL_POSITION, 0f, 0f, 0f)

            AL10.alSourcePlay(sourceId)

            while (AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING) {
                Thread.sleep(10)
            }

            AL10.alDeleteSources(sourceId)
            AL10.alDeleteBuffers(bufferId)
        }
    }
}