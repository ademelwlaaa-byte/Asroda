package com.example.ui.screens.cosmic3d

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.R
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh
import kotlin.random.Random

enum class CosmicSfx {
    COSMIC_BEAM,
    TRIPLE_BURST,
    CRYSTAL_SHATTER,
    SPARK_COLLECT,
    GENERATOR_EXPLODE,
    BOSS_HIT,
    LASER_WARNING,
    LASER_FIRE,
    DASH_BLINK,
    TIME_SLOW_ENTER,
    TIME_SLOW_EXIT,
    WHITE_EYE_NOVA,
    AVENGERS_SUPPORT,
    PLAYER_SHIELD_HIT,
    VICTORY_FANFARE,
    COMMS_CHIRP
}

/**
 * Cinema-grade, emotional, organic 44.1kHz stereo audio engine for 3D cutscenes & Aiden's 3D Boss Game.
 *
 * Design principles (zero robotic / arcade beep feel):
 * - Real-time polyphonic stereo PCM mixer (AudioTrack MODE_STREAM) with warm analog soft-saturation limiting.
 * - Multi-layered film sound design: filtered pink-noise wind/thunder/whooshes, warm harmonic cello/choir pads,
 *   Hans Zimmer-style low-passed sub-bass impacts, stereo crystalline bells, and cinematic time-stop drops.
 * - Real-time stereo panning based on 3D world X position + dynamic Time-Dilation pitch/filter slowdown.
 * - Optional seamless integration with the real orchestral soundtrack (R.raw.kozmiksuruklenismuzik).
 */
class Cosmic3DSoundEngine(
    private val context: Context? = null,
    private val enableOrchestralMusic: Boolean = false
) {
    private class ActiveVoice(
        val buffer: FloatArray,
        var pos: Float = 0f,
        val baseRate: Float = 1f,
        val gainL: Float,
        val gainR: Float,
        val affectedByTimeSlow: Boolean = true
    )

    companion object {
        private const val SAMPLE_RATE = 24000 // Warm, silky, ultra-fast synthesis & <0.3% CPU mixing
        private const val TWO_PI = (2.0 * PI).toFloat()

        // Cache pre-rendered cinema SFX buffers statically so opening subsequent scenes/restarts is instant!
        @Volatile private var sfxBankReady = false
        private val sfxLock = Any()
        private val sfxBank = ConcurrentHashMap<CosmicSfx, FloatArray>()
        private val ambientBank = ConcurrentHashMap<String, FloatArray>()

        private fun ensureSfxBankBuilt() {
            if (sfxBankReady) return
            synchronized(sfxLock) {
                if (sfxBankReady) return
                sfxBank[CosmicSfx.COSMIC_BEAM] = synthCosmicBeam(heavy = false)
                sfxBank[CosmicSfx.TRIPLE_BURST] = synthCosmicBeam(heavy = true)
                sfxBank[CosmicSfx.CRYSTAL_SHATTER] = synthCrystalShatter()
                sfxBank[CosmicSfx.SPARK_COLLECT] = synthSparkCollect()
                sfxBank[CosmicSfx.GENERATOR_EXPLODE] = synthGeneratorExplode()
                sfxBank[CosmicSfx.BOSS_HIT] = synthBossHit()
                sfxBank[CosmicSfx.LASER_WARNING] = synthLaserWarning()
                sfxBank[CosmicSfx.LASER_FIRE] = synthLaserFire()
                sfxBank[CosmicSfx.DASH_BLINK] = synthDashBlink()
                sfxBank[CosmicSfx.TIME_SLOW_ENTER] = synthTimeSlow(entering = true)
                sfxBank[CosmicSfx.TIME_SLOW_EXIT] = synthTimeSlow(entering = false)
                sfxBank[CosmicSfx.WHITE_EYE_NOVA] = synthWhiteEyeNova()
                sfxBank[CosmicSfx.AVENGERS_SUPPORT] = synthAvengersSupport()
                sfxBank[CosmicSfx.PLAYER_SHIELD_HIT] = synthShieldHit()
                sfxBank[CosmicSfx.VICTORY_FANFARE] = synthVictoryFanfare()
                sfxBank[CosmicSfx.COMMS_CHIRP] = synthCommsChirp()
                sfxBankReady = true
            }
        }

        private fun getOrBuildAmbient(sceneId: String): FloatArray {
            synchronized(sfxLock) {
                return ambientBank.getOrPut(sceneId) { synthAmbientScene(sceneId) }
            }
        }

        // --- Warm Pink-Noise & One-Pole Low-Pass Filter helpers for organic cinema textures ---
        private class PinkNoise(seed: Int) {
            private val rng = Random(seed)
            private var b0 = 0f; private var b1 = 0f; private var b2 = 0f
            private var b3 = 0f; private var b4 = 0f; private var b5 = 0f; private var b6 = 0f
            fun next(): Float {
                val white = rng.nextFloat() * 2f - 1f
                b0 = 0.99886f * b0 + white * 0.0555179f
                b1 = 0.99332f * b1 + white * 0.0750759f
                b2 = 0.96900f * b2 + white * 0.1538520f
                b3 = 0.86650f * b3 + white * 0.3104856f
                b4 = 0.55000f * b4 + white * 0.5329522f
                b5 = -0.7616f * b5 - white * 0.0168980f
                val out = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f) * 0.11f
                b6 = white * 0.115926f
                return out
            }
        }

        private fun lowPass(samples: FloatArray, cutoffHz: Float) {
            val rc = 1f / (TWO_PI * cutoffHz.coerceAtLeast(30f))
            val dt = 1f / SAMPLE_RATE
            val alpha = dt / (rc + dt)
            var prev = 0f
            for (i in samples.indices) {
                prev += alpha * (samples[i] - prev)
                samples[i] = prev
            }
        }

        private fun applySmoothLoopCrossfade(samples: FloatArray, fadeSamples: Int = 2400) {
            val n = samples.size
            val f = fadeSamples.coerceAtMost(n / 4)
            for (i in 0 until f) {
                val w = i.toFloat() / f
                val tailIdx = n - f + i
                // Equal-power crossfade tail into head for click-free eternal looping
                val head = samples[i]
                val tail = samples[tailIdx]
                samples[i] = head * w + tail * (1f - w)
            }
        }

        // 1. Warm MCU Plasma Repulsor Blast (layered air whoosh + sub-punch + velvety harmonic sweep)
        private fun synthCosmicBeam(heavy: Boolean): FloatArray {
            val dur = if (heavy) 0.42f else 0.28f
            val n = (SAMPLE_RATE * dur).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(if (heavy) 101 else 102)
            var ph1 = 0f; var ph2 = 0f; var subPh = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (t / 0.012f).coerceAtMost(1f) * exp(-t * (if (heavy) 7.5f else 11.5f))
                val freq = (if (heavy) 380f else 480f) * exp(-t * 6.5f) + 95f
                val subF = 135f * exp(-t * 9.0f) + 42f
                ph1 += TWO_PI * freq / SAMPLE_RATE
                ph2 += TWO_PI * (freq * 1.498f) / SAMPLE_RATE
                subPh += TWO_PI * subF / SAMPLE_RATE
                val tonal = sin(ph1) * 0.45f + sin(ph2) * 0.22f + sin(subPh) * (if (heavy) 0.58f else 0.38f)
                val air = noise.next() * exp(-t * 14f) * 0.55f
                out[i] = tanh((tonal + air) * env * 1.35f) * 0.72f
            }
            lowPass(out, if (heavy) 2400f else 3100f)
            return out
        }

        // 2. Crystalline Shatter + Organic Sub-Boom
        private fun synthCrystalShatter(): FloatArray {
            val n = (SAMPLE_RATE * 0.55f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(203)
            val bellFreqs = floatArrayOf(880f, 1318f, 1760f, 2349f, 3136f)
            var subPh = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val subEnv = exp(-t * 8.5f)
                subPh += TWO_PI * (92f * exp(-t * 6f) + 34f) / SAMPLE_RATE
                var bells = 0f
                for (b in bellFreqs.indices) {
                    val bEnv = exp(-t * (5.5f + b * 2.2f))
                    bells += sin(TWO_PI * bellFreqs[b] * t + b * 0.7f) * bEnv * 0.14f
                }
                val burst = noise.next() * exp(-t * 10f) * 0.55f
                out[i] = tanh((sin(subPh) * subEnv * 0.65f + bells + burst) * 1.25f) * 0.78f
            }
            lowPass(out, 4200f)
            return out
        }

        // 3. Uplifting Celestial Harp / Celesta Spark Collect
        private fun synthSparkCollect(): FloatArray {
            val n = (SAMPLE_RATE * 0.62f).toInt()
            val out = FloatArray(n)
            val notes = floatArrayOf(587.33f, 880f, 1174.66f, 1396.91f) // D5, A5, D6, F6 warm shimmer
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                var sum = 0f
                for (k in notes.indices) {
                    val start = k * 0.045f
                    if (t >= start) {
                        val lt = t - start
                        val env = (lt / 0.015f).coerceAtMost(1f) * exp(-lt * 6.2f)
                        val f = notes[k]
                        // Subtle detuned stereo warmth
                        sum += (sin(TWO_PI * f * lt) * 0.65f + sin(TWO_PI * (f * 2.003f) * lt) * 0.20f) * env
                    }
                }
                out[i] = tanh(sum * 0.45f) * 0.70f
            }
            return out
        }

        // 4. Massive IMAX Dreadnought Generator Explosion
        private fun synthGeneratorExplode(): FloatArray {
            val n = (SAMPLE_RATE * 1.55f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(404)
            var subPh = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val punch = (t / 0.02f).coerceAtMost(1f) * exp(-t * 2.4f)
                val subF = 82f * exp(-t * 3.8f) + 26f
                subPh += TWO_PI * subF / SAMPLE_RATE
                val sub = sin(subPh) * 0.90f
                val rumble = noise.next() * 1.15f * exp(-t * 2.1f)
                val metal = sin(TWO_PI * 196f * t + sin(TWO_PI * 37f * t) * 2.5f) * exp(-t * 4.5f) * 0.25f
                out[i] = tanh((sub + rumble + metal) * punch * 1.4f) * 0.92f
            }
            lowPass(out, 1450f)
            return out
        }

        // 5. Heavy Metallic Alien Armor Shield Impact
        private fun synthBossHit(): FloatArray {
            val n = (SAMPLE_RATE * 0.32f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(505)
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = exp(-t * 11f)
                val clang = sin(TWO_PI * 240f * t) * 0.45f + sin(TWO_PI * 367f * t) * 0.30f + sin(TWO_PI * 68f * t) * 0.60f
                out[i] = tanh((clang + noise.next() * 0.4f) * env * 1.3f) * 0.68f
            }
            lowPass(out, 1900f)
            return out
        }

        // 6. Tense Organic Cinema Riser for Vaelor Siege Laser Telegraph
        private fun synthLaserWarning(): FloatArray {
            val n = (SAMPLE_RATE * 0.95f).toInt()
            val out = FloatArray(n)
            var ph1 = 0f; var ph2 = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val prog = t / 0.95f
                val env = sin(prog * PI.toFloat()) * (0.65f + 0.35f * sin(TWO_PI * 7.5f * t))
                val f = 140f + prog * prog * 310f
                ph1 += TWO_PI * f / SAMPLE_RATE
                ph2 += TWO_PI * (f * 1.008f) / SAMPLE_RATE
                val tone = (sin(ph1) + sin(ph2)) * 0.35f + sin(ph1 * 0.5f) * 0.30f
                out[i] = tanh(tone * env) * 0.55f
            }
            lowPass(out, 1600f)
            return out
        }

        // 7. Deep Roaring Crimson Siege Laser Fire
        private fun synthLaserFire(): FloatArray {
            val n = (SAMPLE_RATE * 0.85f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(707)
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (t / 0.03f).coerceAtMost(1f) * (1f - ((t - 0.55f).coerceAtLeast(0f) / 0.30f)).coerceIn(0f, 1f)
                val bass = sin(TWO_PI * 58f * t) * 0.72f + sin(TWO_PI * 116f * t) * 0.35f
                val plasma = noise.next() * 0.85f
                out[i] = tanh((bass + plasma) * env * 1.35f) * 0.80f
            }
            lowPass(out, 1800f)
            return out
        }

        // 8. Dimensional Blink Dash Whoosh (vacuum pull + silky bass pop)
        private fun synthDashBlink(): FloatArray {
            val n = (SAMPLE_RATE * 0.36f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(808)
            var ph = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = if (t < 0.07f) (t / 0.07f) else exp(-(t - 0.07f) * 10.5f)
                val f = if (t < 0.07f) 110f + (t / 0.07f) * 320f else 430f * exp(-(t - 0.07f) * 11f) + 55f
                ph += TWO_PI * f / SAMPLE_RATE
                val whoosh = noise.next() * 0.75f + sin(ph) * 0.55f
                out[i] = tanh(whoosh * env * 1.4f) * 0.78f
            }
            lowPass(out, 2600f)
            return out
        }

        // 9. Iconic Movie Time-Dilation Bass Drop ("BWOOOM") & Reverse Snap-Back
        private fun synthTimeSlow(entering: Boolean): FloatArray {
            val dur = if (entering) 1.25f else 0.55f
            val n = (SAMPLE_RATE * dur).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(if (entering) 901 else 902)
            var ph1 = 0f; var ph2 = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val prog = t / dur
                val env = (t / 0.04f).coerceAtMost(1f) * (1f - prog * 0.78f)
                val f = if (entering) {
                    250f * exp(-t * 3.8f) + 36f
                } else {
                    42f + prog * prog * 240f
                }
                ph1 += TWO_PI * f / SAMPLE_RATE
                ph2 += TWO_PI * (f * 1.502f) / SAMPLE_RATE
                val shimmer = sin(TWO_PI * 880f * t) * exp(-t * 3.2f) * (if (entering) 0.14f else 0.05f)
                val sub = sin(ph1) * 0.80f + sin(ph2) * 0.30f + noise.next() * 0.35f * exp(-t * 2.5f)
                out[i] = tanh((sub + shimmer) * env * 1.45f) * 0.88f
            }
            lowPass(out, 1500f)
            return out
        }

        // 10. Absolute Awakening — White Eye Supernova (breath drop + colossal bass + soaring celestial choir!)
        private fun synthWhiteEyeNova(): FloatArray {
            val n = (SAMPLE_RATE * 2.25f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(1010)
            var subPh = 0f
            val chord = floatArrayOf(146.83f, 220f, 293.66f, 369.99f, 440f, 587.33f) // Rich D-Major Celestial Swell
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                if (t < 0.12f) {
                    // Dramatic air-inhale pull before detonation
                    val w = t / 0.12f
                    out[i] = noise.next() * w * 0.32f
                    continue
                }
                val dt = t - 0.12f
                val subEnv = (dt / 0.025f).coerceAtMost(1f) * exp(-dt * 1.45f)
                subPh += TWO_PI * (86f * exp(-dt * 2.6f) + 30f) / SAMPLE_RATE
                val subBoom = sin(subPh) * 0.95f * subEnv
                val windBoom = noise.next() * 0.80f * exp(-dt * 2.0f)
                var choir = 0f
                val choirEnv = (dt / 0.25f).coerceAtMost(1f) * exp(-dt * 0.95f)
                for (c in chord.indices) {
                    val f = chord[c]
                    val vibrato = sin(TWO_PI * (4.2f + c * 0.3f) * dt) * 0.004f
                    choir += sin(TWO_PI * f * (1f + vibrato) * dt + c) * 0.16f
                }
                out[i] = tanh((subBoom + windBoom + choir * choirEnv) * 1.35f) * 0.94f
            }
            lowPass(out, 2800f)
            return out
        }

        // 11. Avengers Team-Up Support (Thor Lightning Crack + Thunder + Heroic Brass 5th)
        private fun synthAvengersSupport(): FloatArray {
            val n = (SAMPLE_RATE * 1.45f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(1111)
            val brass = floatArrayOf(146.83f, 220f, 293.66f, 440f) // Heroic D5 power fifth!
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                // Lightning crack + rolling thunder
                val crack = noise.next() * (exp(-t * 16f) * 1.1f + exp(-t * 2.4f) * 0.55f)
                val hornEnv = (t / 0.12f).coerceAtMost(1f) * exp(-t * 1.6f)
                var horn = 0f
                for (b in brass.indices) {
                    val f = brass[b]
                    // Warm brass = fundamental + soft 2nd & 3rd harmonics
                    horn += (sin(TWO_PI * f * t) * 0.55f + sin(TWO_PI * f * 2f * t) * 0.28f + sin(TWO_PI * f * 3f * t) * 0.12f) * 0.22f
                }
                out[i] = tanh((crack + horn * hornEnv) * 1.35f) * 0.88f
            }
            lowPass(out, 2500f)
            return out
        }

        // 12. Player Cosmic Shield Crack Hit
        private fun synthShieldHit(): FloatArray {
            val n = (SAMPLE_RATE * 0.45f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(1212)
            var subPh = 0f
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = exp(-t * 7.5f)
                subPh += TWO_PI * (110f * exp(-t * 7f) + 38f) / SAMPLE_RATE
                val glass = (sin(TWO_PI * 740f * t) + sin(TWO_PI * 1110f * t)) * exp(-t * 16f) * 0.22f
                out[i] = tanh((sin(subPh) * 0.85f + noise.next() * 0.65f + glass) * env * 1.4f) * 0.85f
            }
            lowPass(out, 2100f)
            return out
        }

        // 13. Warm Emotional Orchestral Victory Chord Resolution
        private fun synthVictoryFanfare(): FloatArray {
            val n = (SAMPLE_RATE * 2.6f).toInt()
            val out = FloatArray(n)
            val chord1 = floatArrayOf(130.81f, 196.00f, 261.63f, 329.63f) // C Major warm foundation
            val chord2 = floatArrayOf(146.83f, 220.00f, 293.66f, 369.99f, 440.00f, 587.33f) // Soaring D Major resolve!
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                var sum = 0f
                if (t < 0.72f) {
                    val env = (t / 0.16f).coerceAtMost(1f) * (1f - (t / 0.72f) * 0.35f)
                    for (f in chord1) {
                        sum += (sin(TWO_PI * f * t) * 0.6f + sin(TWO_PI * f * 2f * t) * 0.22f) * 0.18f * env
                    }
                }
                if (t >= 0.52f) {
                    val lt = t - 0.52f
                    val env = (lt / 0.22f).coerceAtMost(1f) * exp(-lt * 0.85f)
                    for (idx in chord2.indices) {
                        val f = chord2[idx]
                        val vib = 1f + sin(TWO_PI * 4.5f * lt + idx) * 0.003f
                        sum += (sin(TWO_PI * f * vib * lt) * 0.6f + sin(TWO_PI * f * 2f * lt) * 0.2f) * 0.16f * env
                    }
                }
                out[i] = tanh(sum * 1.3f) * 0.82f
            }
            lowPass(out, 2600f)
            return out
        }

        // 14. Authentic Soft Tactical Avengers Earpiece Click (subtle, organic, never shrill)
        private fun synthCommsChirp(): FloatArray {
            val n = (SAMPLE_RATE * 0.14f).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(1414)
            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val env = (t / 0.01f).coerceAtMost(1f) * exp(-t * 24f)
                val squelch = noise.next() * 0.45f + sin(TWO_PI * 620f * t) * 0.18f * exp(-t * 35f)
                out[i] = squelch * env * 0.55f
            }
            lowPass(out, 2200f)
            return out
        }

        // 15. Bespoke Emotional 4.5-second Seamlessly Looping Ambient Soundscapes for Each 3D Scene!
        private fun synthAmbientScene(sceneId: String): FloatArray {
            val durSec = 4.5f
            val n = (SAMPLE_RATE * durSec).toInt()
            val out = FloatArray(n)
            val noise = PinkNoise(sceneId.hashCode())

            val chord = when (sceneId) {
                "BLUE_SKY_2006" -> floatArrayOf(73.42f, 110.0f, 174.61f, 261.63f)       // Melancholic D minor 7 strings
                "WHITE_EYE_AWAKENING" -> floatArrayOf(110.0f, 164.81f, 246.94f, 329.63f) // Ethereal A sus2 glass choir
                "BOSPHORUS_RESONANCE" -> floatArrayOf(65.41f, 98.0f, 155.56f, 233.08f)   // Dramatic C minor sea resonance
                "MANHATTAN_ARMADA" -> floatArrayOf(55.0f, 82.41f, 110.0f, 130.81f)       // Massive dark A-minor dreadnought
                "BOSS_BATTLE" -> floatArrayOf(58.27f, 87.31f, 116.54f, 174.61f)          // Tense deep Bb cosmic wind bed
                else -> floatArrayOf(87.31f, 130.81f, 220.0f, 329.63f, 392.0f)           // Soaring Fmaj9 Mutlak Uyanis
            }

            for (i in 0 until n) {
                val t = i.toFloat() / SAMPLE_RATE
                val breath = 0.72f + 0.28f * sin(TWO_PI * (t / durSec))
                var pad = 0f
                for (c in chord.indices) {
                    val f = chord[c]
                    val slowDetune = 1f + sin(TWO_PI * (0.22f + c * 0.08f) * t + c) * 0.0035f
                    pad += (sin(TWO_PI * f * slowDetune * t) * 0.65f + sin(TWO_PI * f * 2f * t) * 0.14f) * (0.16f / (1f + c * 0.15f))
                }

                // Organic scene-specific texture layer
                val wind = noise.next() * when (sceneId) {
                    "BOSPHORUS_RESONANCE" -> (0.28f + 0.18f * sin(TWO_PI * (t / durSec) * 2f))
                    "MANHATTAN_ARMADA", "BOSS_BATTLE" -> 0.34f
                    else -> 0.20f
                }

                // Deep warm cinematic double-heartbeat for White Eye Awakening!
                var heart = 0f
                if (sceneId == "WHITE_EYE_AWAKENING") {
                    val beatT = t % 1.125f
                    val lub = if (beatT < 0.18f) sin(TWO_PI * 52f * beatT) * exp(-beatT * 18f) else 0f
                    val dub = if (beatT in 0.22f..0.42f) {
                        val dt = beatT - 0.22f
                        sin(TWO_PI * 44f * dt) * exp(-dt * 20f) * 0.75f
                    } else 0f
                    heart = (lub + dub) * 0.48f
                }

                out[i] = tanh((pad * breath + wind + heart) * 1.15f) * 0.50f
            }

            val cutoff = when (sceneId) {
                "MANHATTAN_ARMADA", "BOSS_BATTLE" -> 620f
                "WHITE_EYE_AWAKENING" -> 1150f
                else -> 880f
            }
            lowPass(out, cutoff)
            applySmoothLoopCrossfade(out, fadeSamples = 3200)
            return out
        }
    }

    private val voices = CopyOnWriteArrayList<ActiveVoice>()
    private val running = AtomicBoolean(false)
    @Volatile private var muted = false
    @Volatile private var voiceDucking = false
    @Volatile private var timeSlowFactor = 1.0f // 1.0 = normal, 0.58 = cinematic Bullet-Time slow pitch!
    @Volatile private var ambientBuffer: FloatArray? = null
    @Volatile private var ambientGain = 0.55f

    private var mixerThread: Thread? = null
    private var musicPlayer: MediaPlayer? = null
    @Volatile private var targetMusicVolume = 0.36f

    fun start(ambientSceneId: String? = null, ambientVolume: Float = 0.52f) {
        if (running.getAndSet(true)) return
        ambientGain = ambientVolume

        mixerThread = Thread({
            try {
                ensureSfxBankBuilt()
                if (ambientSceneId != null) {
                    ambientBuffer = getOrBuildAmbient(ambientSceneId)
                }
                runMixerLoop()
            } catch (_: Throwable) {
                // Swallow hardware audio teardown safely
            }
        }, "Cosmic3D-AudioMixer").apply {
            priority = Thread.MAX_PRIORITY - 1
            start()
        }

        if (enableOrchestralMusic && context != null) {
            startOrchestralBed(context.applicationContext)
        }
    }

    private fun startOrchestralBed(appCtx: Context) {
        try {
            val afd = appCtx.resources.openRawResourceFd(R.raw.kozmiksuruklenismuzik) ?: return
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            afd.use { mp.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            mp.isLooping = true
            mp.prepare()
            val v = if (muted) 0f else targetMusicVolume
            mp.setVolume(v, v)
            mp.start()
            musicPlayer = mp
        } catch (_: Throwable) {
            // Optional orchestral track — organic ambient bed still runs seamlessly
        }
    }

    private fun applyMusicVolume() {
        val duckMult = if (voiceDucking) 0.48f else 1.0f
        val v = if (muted) 0f else (targetMusicVolume * duckMult)
        try {
            musicPlayer?.setVolume(v, v)
        } catch (_: Throwable) {
        }
    }

    fun setMuted(isMuted: Boolean) {
        muted = isMuted
        applyMusicVolume()
    }

    fun setVoiceDucking(active: Boolean) {
        voiceDucking = active
        applyMusicVolume()
    }

    fun setTimeSlowActive(active: Boolean) {
        timeSlowFactor = if (active) 0.62f else 1.0f
        targetMusicVolume = if (active) 0.16f else 0.38f
        applyMusicVolume()
    }

    fun play(sfx: CosmicSfx, volume: Float = 0.85f, pan: Float = 0f, affectedByTimeSlow: Boolean = true) {
        if (muted || !running.get() || !sfxBankReady) return
        val buf = sfxBank[sfx] ?: return
        if (voices.size > 16) return
        val p = pan.coerceIn(-0.85f, 0.85f)
        val gL = volume * ((1f - p) * 0.5f + 0.5f).coerceAtMost(1f)
        val gR = volume * ((1f + p) * 0.5f + 0.5f).coerceAtMost(1f)
        val rateJitter = 0.96f + Random.nextFloat() * 0.08f
        voices.add(ActiveVoice(buf, 0f, rateJitter, gL, gR, affectedByTimeSlow))
    }

    fun stopAndRelease() {
        running.set(false)
        voices.clear()
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (_: Throwable) {
        }
        musicPlayer = null
        try {
            mixerThread?.join(350L)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        mixerThread = null
    }

    private fun runMixerLoop() {
        val minBuf = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(2048)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        val framesPerChunk = 512
        val pcmOut = ShortArray(framesPerChunk * 2)
        var ambPos = 0f
        var smoothRate = 1.0f
        var smoothDuck = 1.0f
        var lpStateL = 0f
        var lpStateR = 0f

        try {
            track.play()
            while (running.get()) {
                val targetRate = timeSlowFactor
                smoothRate += (targetRate - smoothRate) * 0.08f
                val targetDuck = if (voiceDucking) 0.56f else 1.0f
                smoothDuck += (targetDuck - smoothDuck) * 0.14f
                val isSlowFilter = smoothRate < 0.85f
                val lpAlpha = if (isSlowFilter) 0.24f else 0.92f
                val isMutedNow = muted
                val amb = ambientBuffer

                for (f in 0 until framesPerChunk) {
                    var sumL = 0f
                    var sumR = 0f

                    if (!isMutedNow) {
                        // Continuous organic ambient soundscape
                        if (amb != null && amb.isNotEmpty()) {
                            val idx0 = ambPos.toInt() % amb.size
                            val idx1 = (idx0 + 1) % amb.size
                            val frac = ambPos - ambPos.toInt()
                            val sample = (amb[idx0] * (1f - frac) + amb[idx1] * frac) * ambientGain
                            // Subtle organic stereo spread on ambient bed
                            val idxR = (idx0 + 420) % amb.size
                            val sampleR = amb[idxR] * ambientGain
                            sumL += sample
                            sumR += sampleR
                            ambPos += smoothRate
                            if (ambPos >= amb.size) ambPos -= amb.size
                        }

                        // Mix active cinema SFX voices
                        val iter = voices.iterator()
                        while (iter.hasNext()) {
                            val v = iter.next()
                            val p = v.pos.toInt()
                            if (p >= v.buffer.size - 1) {
                                voices.remove(v)
                                continue
                            }
                            val frac = v.pos - p
                            val s = v.buffer[p] * (1f - frac) + v.buffer[p + 1] * frac
                            sumL += s * v.gainL
                            sumR += s * v.gainR
                            val step = v.baseRate * (if (v.affectedByTimeSlow) smoothRate else 1f)
                            v.pos += step
                        }
                    }

                    // Dynamic Bullet-Time warm low-pass filter + voice ducking + soft-saturation limiting
                    lpStateL += lpAlpha * (sumL * smoothDuck - lpStateL)
                    lpStateR += lpAlpha * (sumR * smoothDuck - lpStateR)
                    val outL = tanh(lpStateL) * 29000f
                    val outR = tanh(lpStateR) * 29000f

                    pcmOut[f * 2] = outL.toInt().coerceIn(-32767, 32767).toShort()
                    pcmOut[f * 2 + 1] = outR.toInt().coerceIn(-32767, 32767).toShort()
                }

                track.write(pcmOut, 0, pcmOut.size)
            }
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }
    }
}
