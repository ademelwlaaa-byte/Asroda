package com.example.ui.screens.cosmic3d

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.DrawableRes
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

internal fun safeFiniteFloat(value: Float, fallback: Float = 0f): Float =
    if (value.isFinite()) value else fallback

internal data class PanoramicViewportBounds(
    val originX: Float,
    val originY: Float,
    val panW: Float,
    val panH: Float
)

/**
 * Computes full-bleed 16:9 panoramic projection bounds mathematically clamped inside the viewport
 * so zero black bars or NaN/Infinity coordinates can ever occur.
 */
internal fun computePanoramicViewportBounds(
    canvasW: Float,
    canvasH: Float,
    bmpW: Float,
    bmpH: Float,
    camYawDeg: Float,
    camPitchDeg: Float,
    leanZoom: Float,
    turnProgress: Float,
    charCenterXLookout: Float,
    charCenterXTurned: Float,
    headCenterY: Float
): PanoramicViewportBounds? {
    if (!canvasW.isFinite() || !canvasH.isFinite() || canvasW <= 1f || canvasH <= 1f) return null
    val safeBmpW = safeFiniteFloat(bmpW, 1f).coerceAtLeast(1f)
    val safeBmpH = safeFiniteFloat(bmpH, 1f).coerceAtLeast(1f)
    val bmpAspect = (safeBmpW / safeBmpH).coerceIn(0.25f, 4.0f)

    val safeZoom = safeFiniteFloat(leanZoom, 1f).coerceIn(0.95f, 2.0f)
    val minCoverH = max(canvasH, canvasW / bmpAspect)
    val panH = max(minCoverH, minCoverH * 1.06f * safeZoom)
    val panW = panH * bmpAspect

    val t = safeFiniteFloat(turnProgress, 0f).coerceIn(0f, 1f)
    val activeCharCenterX = (safeFiniteFloat(charCenterXLookout, 0.5f) +
        (safeFiniteFloat(charCenterXTurned, 0.5f) - safeFiniteFloat(charCenterXLookout, 0.5f)) * t)
        .coerceIn(0f, 1f)

    val minOriginX = (canvasW - panW).coerceAtMost(0f)
    val maxOriginX = 0f
    val homeOriginX = (canvasW * 0.50f - activeCharCenterX * panW).coerceIn(minOriginX, maxOriginX)

    val yawNorm = (safeFiniteFloat(camYawDeg, 0f) / 78f).coerceIn(-1f, 1f)
    val rawOriginX = if (yawNorm < 0f) {
        homeOriginX + (-yawNorm) * (maxOriginX - homeOriginX)
    } else {
        homeOriginX + yawNorm * (minOriginX - homeOriginX)
    }
    val originX = rawOriginX.coerceIn(minOriginX, maxOriginX)

    val minOriginY = (canvasH - panH).coerceAtMost(0f)
    val maxOriginY = 0f
    val desiredHeadScreenY = canvasH * 0.31f
    val safeHeadY = safeFiniteFloat(headCenterY, 0.31f).coerceIn(0f, 1f)
    val homeOriginY = (desiredHeadScreenY - safeHeadY * panH).coerceIn(minOriginY, maxOriginY)
    val pitchNorm = (safeFiniteFloat(camPitchDeg, 0f) / 18f).coerceIn(-1f, 1f)
    val pitchRange = abs(minOriginY) * 0.44f
    val originY = (homeOriginY - pitchNorm * pitchRange).coerceIn(minOriginY, maxOriginY)

    return PanoramicViewportBounds(originX = originX, originY = originY, panW = panW, panH = panH)
}

/**
 * Calibrated normalized [0..1] anatomical coordinates on a pixel-locked 16:9 companion frame set.
 * Used to extract tight, invisibly feathered real-photograph patches for closed eyelids, speaking
 * mouth/jaw, and optional wind-swept hair without any jelly distortion on the base scene.
 */
data class LivingFaceRigSpec(
    val charCenterXLookout: Float,
    val charCenterXTurned: Float,
    val headCenterX: Float,
    val headCenterY: Float,
    val headRadiusX: Float,
    val headRadiusY: Float,
    val leftEyeX: Float,
    val leftEyeY: Float,
    val rightEyeX: Float,
    val rightEyeY: Float,
    val eyePatchRx: Float,
    val eyePatchRy: Float,
    val mouthCenterX: Float,
    val mouthCenterY: Float,
    val mouthPatchRx: Float,
    val mouthPatchRy: Float,
    val hairMinX: Float,
    val hairMaxX: Float,
    val hairMinY: Float,
    val hairMaxY: Float,
    val hasLongFlowingHair: Boolean,
    val turnSign: Float, // +1f = turns head rightward to face camera, -1f = turns leftward
    val windDir: Float   // -1f = wind blows left, +1f = wind blows right
)

/**
 * Live per-frame viseme state driven by real-time audio RMS and Turkish phoneme analysis.
 */
data class FacialVisemeFrame(
    val jawDrop: Float = 0f,       // 0..1 mouth aperture driving real open-mouth patch
    val lipWidth: Float = 0f,      // -0.35 (rounded OO/UU) .. +0.35 (wide EE/AA)
    val upperLipLift: Float = 0f,  // 0..0.35
    val browLift: Float = 0f,      // 0..0.30
    val cheekSmile: Float = 0f     // 0..0.25
)

/**
 * Lightweight Turkish phoneme-to-viseme stream synchronized with real-time speech RMS amplitude
 * so bilabial stops (m/p/b) and pauses cleanly shut the lips while open vowels (a/e/o/u) open
 * the real photographed mouth smoothly.
 */
object TurkishSpeechVisemeAnalyzer {
    private val TR_LOCALE = Locale.forLanguageTag("tr-TR")

    private enum class VisemeClass(
        val jaw: Float,
        val width: Float,
        val upperLift: Float,
        val brow: Float
    ) {
        REST(0.0f, 0.0f, 0.0f, 0.0f),
        CLOSED_M(0.02f, -0.10f, 0.0f, 0.04f),     // m, p, b
        OPEN_AA(1.00f, 0.24f, 0.28f, 0.20f),      // a, â
        WIDE_EE(0.64f, 0.34f, 0.18f, 0.14f),      // e, i, ı
        ROUND_OO(0.80f, -0.34f, 0.14f, 0.18f),    // o, ö, u, ü
        DENTAL_FF(0.30f, 0.08f, 0.12f, 0.08f),    // f, v, s, ş, z, c, ç
        CONSONANT(0.42f, 0.04f, 0.08f, 0.06f)     // other consonants
    }

    fun sampleViseme(
        dialogueText: String,
        speechElapsedSec: Float,
        audioRms: Float
    ): FacialVisemeFrame {
        if (!speechElapsedSec.isFinite() || !audioRms.isFinite() || audioRms <= 0.03f || dialogueText.isBlank()) {
            return FacialVisemeFrame()
        }
        val clean = dialogueText.lowercase(TR_LOCALE)
        val len = clean.length
        val safeElapsed = speechElapsedSec.coerceAtLeast(0f)
        // Natural Turkish conversational pacing (~13.2 chars/sec)
        val cursor = (safeElapsed * 13.2f) % max(len, 1).toFloat()
        val idx0 = cursor.toInt().coerceIn(0, len - 1)
        val idx1 = (idx0 + 1) % len
        val frac = (cursor - idx0.toFloat()).coerceIn(0f, 1f)

        val v0 = classifyChar(clean[idx0])
        val v1 = classifyChar(clean[idx1])
        val smooth = frac * frac * (3f - 2f * frac)

        val rmsGain = (audioRms * 1.24f).coerceIn(0f, 1f)
        val jaw = lerp(v0.jaw, v1.jaw, smooth) * rmsGain
        val width = lerp(v0.width, v1.width, smooth) * (0.45f + 0.55f * rmsGain)
        val upper = lerp(v0.upperLift, v1.upperLift, smooth) * rmsGain
        val brow = lerp(v0.brow, v1.brow, smooth) * rmsGain * (0.65f + 0.35f * sin(safeElapsed * 2.7f))
        val smile = (if (width > 0.08f) width * 0.45f else 0f) * rmsGain

        return FacialVisemeFrame(
            jawDrop = safeFiniteFloat(jaw).coerceIn(0f, 1f),
            lipWidth = safeFiniteFloat(width).coerceIn(-0.36f, 0.36f),
            upperLipLift = safeFiniteFloat(upper).coerceIn(0f, 0.35f),
            browLift = safeFiniteFloat(brow).coerceIn(0f, 0.32f),
            cheekSmile = safeFiniteFloat(smile).coerceIn(0f, 0.25f)
        )
    }

    private fun classifyChar(ch: Char): VisemeClass = when (ch) {
        ' ', ',', '.', '…', '!', '?', '-', '—' -> VisemeClass.REST
        'm', 'p', 'b' -> VisemeClass.CLOSED_M
        'a', 'â' -> VisemeClass.OPEN_AA
        'e', 'i', 'ı', 'î', 'y' -> VisemeClass.WIDE_EE
        'o', 'ö', 'u', 'ü' -> VisemeClass.ROUND_OO
        'f', 'v', 's', 'ş', 'z', 'c', 'ç' -> VisemeClass.DENTAL_FF
        else -> VisemeClass.CONSONANT
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}

/**
 * A softly elliptical-feathered real-photograph patch extracted from a pixel-locked expression or
 * wind-hair frame, along with its exact normalized [0..1] bounds on the 16:9 panoramic canvas.
 */
class FeatheredPhotoPatch(
    val bitmap: Bitmap,
    val normLeft: Float,
    val normTop: Float,
    val normRight: Float,
    val normBottom: Float
) {
    fun release() {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}

/**
 * Decoded GPU-ready 3-angle head-turn keyframe set (`lookout` -> `midTurn` -> `turned`) plus
 * pixel-locked real-photograph patches for closed eyelids, open speaking mouth/jaw, and optional
 * real wind-swept hair strands. Zero coarse mesh distortion -> zero jelly wobble!
 */
class LivingEncounterTextureSet(
    val lookoutBitmap: Bitmap,
    val midTurnBitmap: Bitmap,
    val turnedBitmap: Bitmap,
    val leftEyeClosedPatch: FeatheredPhotoPatch?,
    val rightEyeClosedPatch: FeatheredPhotoPatch?,
    val mouthOpenPatch: FeatheredPhotoPatch?,
    val windHairPatch: FeatheredPhotoPatch?
) {
    fun release() {
        leftEyeClosedPatch?.release()
        rightEyeClosedPatch?.release()
        mouthOpenPatch?.release()
        windHairPatch?.release()
        if (!lookoutBitmap.isRecycled) lookoutBitmap.recycle()
        if (!midTurnBitmap.isRecycled && midTurnBitmap !== lookoutBitmap && midTurnBitmap !== turnedBitmap) midTurnBitmap.recycle()
        if (!turnedBitmap.isRecycled && turnedBitmap !== lookoutBitmap) turnedBitmap.recycle()
    }
}

object LivingEncounterTextureLoader {
    fun load(
        context: Context,
        @DrawableRes lookoutRes: Int,
        @DrawableRes midTurnRes: Int,
        @DrawableRes turnedRes: Int,
        @DrawableRes exprRes: Int,
        @DrawableRes windHairRes: Int? = null,
        rig: LivingFaceRigSpec
    ): LivingEncounterTextureSet? {
        val lookout = decodeScaled(context, lookoutRes, maxWidth = 1440) ?: return null
        val turned = if (turnedRes == lookoutRes) {
            lookout
        } else {
            decodeScaled(context, turnedRes, maxWidth = 1440) ?: run {
                lookout.recycle()
                return null
            }
        }
        val midTurn = when (midTurnRes) {
            turnedRes -> turned
            lookoutRes -> lookout
            else -> decodeScaled(context, midTurnRes, maxWidth = 1440) ?: turned
        }
        val exprBmp = decodeScaled(context, exprRes, maxWidth = 1440)

        var leftEyePatch: FeatheredPhotoPatch? = null
        var rightEyePatch: FeatheredPhotoPatch? = null
        var mouthPatch: FeatheredPhotoPatch? = null
        var windPatch: FeatheredPhotoPatch? = null

        if (exprBmp != null) {
            leftEyePatch = extractEllipticalFeatheredPatch(
                src = exprBmp,
                cx = rig.leftEyeX,
                cy = rig.leftEyeY,
                rx = rig.eyePatchRx,
                ry = rig.eyePatchRy,
                innerSolidStop = 0.52f,
                outerZeroStop = 0.98f
            )
            rightEyePatch = extractEllipticalFeatheredPatch(
                src = exprBmp,
                cx = rig.rightEyeX,
                cy = rig.rightEyeY,
                rx = rig.eyePatchRx,
                ry = rig.eyePatchRy,
                innerSolidStop = 0.52f,
                outerZeroStop = 0.98f
            )
            mouthPatch = extractEllipticalFeatheredPatch(
                src = exprBmp,
                cx = rig.mouthCenterX,
                cy = rig.mouthCenterY,
                rx = rig.mouthPatchRx,
                ry = rig.mouthPatchRy,
                innerSolidStop = 0.56f,
                outerZeroStop = 0.98f
            )
            // Recycle full expression bitmap immediately after extracting the tiny face patches!
            exprBmp.recycle()
        }

        if (windHairRes != null && rig.hasLongFlowingHair) {
            val windBmp = decodeScaled(context, windHairRes, maxWidth = 1440)
            if (windBmp != null) {
                windPatch = extractWindHairHaloPatch(windBmp, rig)
                windBmp.recycle()
            }
        }

        return LivingEncounterTextureSet(
            lookoutBitmap = lookout,
            midTurnBitmap = midTurn,
            turnedBitmap = turned,
            leftEyeClosedPatch = leftEyePatch,
            rightEyeClosedPatch = rightEyePatch,
            mouthOpenPatch = mouthPatch,
            windHairPatch = windPatch
        )
    }

    private fun decodeScaled(context: Context, @DrawableRes resId: Int, maxWidth: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxWidth) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inScaled = false
        }
        return BitmapFactory.decodeResource(context.resources, resId, opts)
    }

    /**
     * Extracts a rectangular crop from [src] and applies an exact inscribed-ellipse alpha feather
     * (rad = w/2 scaled by h/w) so alpha reaches 0.0 strictly inside all 4 borders -> 0.0px seam!
     */
    private fun extractEllipticalFeatheredPatch(
        src: Bitmap,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        innerSolidStop: Float,
        outerZeroStop: Float
    ): FeatheredPhotoPatch? {
        val normL = (cx - rx).coerceIn(0f, 0.96f)
        val normR = (cx + rx).coerceIn(normL + 0.01f, 1f)
        val normT = (cy - ry).coerceIn(0f, 0.96f)
        val normB = (cy + ry).coerceIn(normT + 0.01f, 1f)

        val pxL = (normL * src.width).toInt().coerceIn(0, src.width - 2)
        val pxT = (normT * src.height).toInt().coerceIn(0, src.height - 2)
        val pxR = (normR * src.width).toInt().coerceIn(pxL + 2, src.width)
        val pxB = (normB * src.height).toInt().coerceIn(pxT + 2, src.height)
        val w = (pxR - pxL).coerceAtLeast(2)
        val h = (pxB - pxT).coerceAtLeast(2)

        val patch = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(patch)
        c.drawBitmap(src, Rect(pxL, pxT, pxR, pxB), Rect(0, 0, w, h), Paint(Paint.FILTER_BITMAP_FLAG))

        val mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val mc = AndroidCanvas(mask)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val mcx = w * 0.5f
        val mcy = h * 0.5f
        val rad = (w * 0.50f).coerceAtLeast(1f)
        mc.save()
        mc.scale(1f, h.toFloat() / w.toFloat().coerceAtLeast(1f), mcx, mcy)
        p.shader = RadialGradient(
            mcx,
            mcy,
            rad,
            intArrayOf(
                AndroidColor.WHITE,
                AndroidColor.WHITE,
                AndroidColor.argb(180, 255, 255, 255),
                AndroidColor.argb(0, 255, 255, 255)
            ),
            floatArrayOf(0f, innerSolidStop, (innerSolidStop + outerZeroStop) * 0.5f, outerZeroStop),
            Shader.TileMode.CLAMP
        )
        mc.drawCircle(mcx, mcy, rad, p)
        mc.restore()

        val xfer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        c.drawBitmap(mask, 0f, 0f, xfer)
        mask.recycle()

        return FeatheredPhotoPatch(
            bitmap = patch,
            normLeft = pxL.toFloat() / src.width.toFloat(),
            normTop = pxT.toFloat() / src.height.toFloat(),
            normRight = pxR.toFloat() / src.width.toFloat(),
            normBottom = pxB.toFloat() / src.height.toFloat()
        )
    }

    /**
     * Extracts ONLY the outer hair halo from a pixel-locked wind-swept hair frame, punching out the
     * inner face oval so eyes, nose, and lips stay 100% untouched while real hair strands blow in gusts!
     */
    private fun extractWindHairHaloPatch(
        src: Bitmap,
        rig: LivingFaceRigSpec
    ): FeatheredPhotoPatch? {
        val normL = rig.hairMinX.coerceIn(0f, 0.92f)
        val normR = rig.hairMaxX.coerceIn(normL + 0.04f, 1f)
        val normT = rig.hairMinY.coerceIn(0f, 0.92f)
        val normB = rig.hairMaxY.coerceIn(normT + 0.04f, 1f)

        val pxL = (normL * src.width).toInt().coerceIn(0, src.width - 2)
        val pxT = (normT * src.height).toInt().coerceIn(0, src.height - 2)
        val pxR = (normR * src.width).toInt().coerceIn(pxL + 2, src.width)
        val pxB = (normB * src.height).toInt().coerceIn(pxT + 2, src.height)
        val w = (pxR - pxL).coerceAtLeast(2)
        val h = (pxB - pxT).coerceAtLeast(2)

        val patch = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = AndroidCanvas(patch)
        c.drawBitmap(src, Rect(pxL, pxT, pxR, pxB), Rect(0, 0, w, h), Paint(Paint.FILTER_BITMAP_FLAG))

        // Step 1: Soft elliptical outer perimeter feather
        val outerMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val omc = AndroidCanvas(outerMask)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val mcx = w * 0.5f
        val mcy = h * 0.5f
        val rad = (w * 0.50f).coerceAtLeast(1f)
        omc.save()
        omc.scale(1f, h.toFloat() / w.toFloat().coerceAtLeast(1f), mcx, mcy)
        p.shader = RadialGradient(
            mcx,
            mcy,
            rad,
            intArrayOf(AndroidColor.WHITE, AndroidColor.WHITE, AndroidColor.argb(0, 255, 255, 255)),
            floatArrayOf(0f, 0.65f, 0.98f),
            Shader.TileMode.CLAMP
        )
        omc.drawCircle(mcx, mcy, rad, p)
        omc.restore()

        val dstIn = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }
        c.drawBitmap(outerMask, 0f, 0f, dstIn)
        outerMask.recycle()

        // Step 2: Punch out the inner face oval (DST_OUT) so face skin, eyes, & mouth never shift!
        val faceCx = ((rig.mouthCenterX - normL) / (normR - normL)) * w
        val faceCy = ((rig.headCenterY + rig.headRadiusY * 0.08f - normT) / (normB - normT)) * h
        val faceRx = (rig.headRadiusX * 0.88f / (normR - normL)) * w
        val faceRy = (rig.headRadiusY * 0.92f / (normB - normT)) * h
        if (faceRx > 2f && faceRy > 2f) {
            val punchMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val pmc = AndroidCanvas(punchMask)
            val pp = Paint(Paint.ANTI_ALIAS_FLAG)
            pmc.save()
            pmc.scale(1f, faceRy / faceRx, faceCx, faceCy)
            pp.shader = RadialGradient(
                faceCx,
                faceCy,
                faceRx,
                intArrayOf(AndroidColor.WHITE, AndroidColor.WHITE, AndroidColor.argb(0, 255, 255, 255)),
                floatArrayOf(0f, 0.68f, 1.0f),
                Shader.TileMode.CLAMP
            )
            pmc.drawCircle(faceCx, faceCy, faceRx, pp)
            pmc.restore()
            val dstOut = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT) }
            c.drawBitmap(punchMask, 0f, 0f, dstOut)
            punchMask.recycle()
        }

        return FeatheredPhotoPatch(
            bitmap = patch,
            normLeft = pxL.toFloat() / src.width.toFloat(),
            normTop = pxT.toFloat() / src.height.toFloat(),
            normRight = pxR.toFloat() / src.width.toFloat(),
            normBottom = pxB.toFloat() / src.height.toFloat()
        )
    }
}

/**
 * Cinema-Grade Pixel-Locked Multi-Keyframe 3D Living Encounter Renderer.
 *
 * Guarantees:
 * 1. ZERO BLACK BARS: Clamps full 16:9 panoramic projection mathematically inside viewport bounds.
 * 2. ZERO JELLY / WATER WOBBLE: Renders razor-sharp rigid base photography (`canvas.drawBitmap`),
 *    never warping straight railings, buildings, clothes, or faces with sine-wave grids!
 * 3. REAL 3-KEYFRAME HEAD-TURN ANIMATION: Smoothly sequences pixel-locked `0° Lookout -> 45° Mid-Turn
 *    -> 90° Eye Contact` frames so ONLY the character's head physically rotates over their shoulder!
 * 4. PHOTOREALISTIC REAL BLINKING: Top-to-bottom natural shutter reveal of real pixel-locked closed
 *    eyelids & eyelashes from the character's `expr` photograph (ZERO stickers, ZERO smear!).
 * 5. PHOTOREALISTIC REAL LIP-SYNC: Real pixel-locked open mouth, teeth, and jaw from the character's
 *    `expr` photograph articulated by Turkish visemes & live audio RMS (ZERO painted ovals!).
 */
class Living3DEncounterMeshRenderer {
    private val crispPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dstRect = RectF()
    private val patchRect = RectF()
    private val clipRect = RectF()

    data class FrameParams(
        val canvasW: Float,
        val canvasH: Float,
        val camYawDeg: Float,       // -78 .. +78 first-person head rotation
        val camPitchDeg: Float,     // -18 .. +18
        val leanZoom: Float,        // 1.0 (normal) .. 1.30 (user tapped "Yaklaş")
        val turnProgress: Float,    // 0.0 (0° scenery lookout) .. 0.5 (45° mid-turn) .. 1.0 (90° eye contact)
        val breathPhase: Float,     // 0 .. 1
        val windTimeSec: Float,
        val windGust: Float,        // 0.08 .. 1.0
        val blink: Float,           // 0 .. 1 strictly synchronized for BOTH eyes simultaneously
        val gazeX: Float,           // -1 .. +1
        val gazeY: Float,           // -1 .. +1
        val speakingNodDeg: Float,  // -3 .. +3
        val viseme: FacialVisemeFrame
    )

    fun drawScene(
        canvas: AndroidCanvas,
        textures: LivingEncounterTextureSet,
        rig: LivingFaceRigSpec,
        params: FrameParams
    ) {
        if (textures.lookoutBitmap.isRecycled || textures.midTurnBitmap.isRecycled || textures.turnedBitmap.isRecycled) {
            return
        }
        val bounds = computePanoramicViewportBounds(
            canvasW = params.canvasW,
            canvasH = params.canvasH,
            bmpW = textures.turnedBitmap.width.toFloat(),
            bmpH = textures.turnedBitmap.height.toFloat(),
            camYawDeg = params.camYawDeg,
            camPitchDeg = params.camPitchDeg,
            leanZoom = params.leanZoom,
            turnProgress = params.turnProgress,
            charCenterXLookout = rig.charCenterXLookout,
            charCenterXTurned = rig.charCenterXTurned,
            headCenterY = rig.headCenterY
        ) ?: return

        val originX = bounds.originX
        val originY = bounds.originY
        val panW = bounds.panW
        val panH = bounds.panH
        val t = safeFiniteFloat(params.turnProgress, 0f).coerceIn(0f, 1f)
        val safeWindGust = safeFiniteFloat(params.windGust, 0f).coerceIn(0f, 1f)
        val safeBlink = safeFiniteFloat(params.blink, 0f).coerceIn(0f, 1f)

        dstRect.set(originX, originY, originX + panW, originY + panH)

        // ── PASS 1: ROCK-SOLID CRISP 3-KEYFRAME REAL HEAD TURN (0° -> 45° -> 90°, ZERO JELLY WOBBLE!) ──
        crispPaint.alpha = 255
        when {
            t <= 0.002f -> {
                canvas.drawBitmap(textures.lookoutBitmap, null, dstRect, crispPaint)
            }
            t < 0.50f -> {
                // Stage 1: 0° Side Profile -> 45° Oblique Mid-Turn (pixel-locked body & skyline!)
                val p = smoothStep(0.04f, 0.96f, t / 0.50f)
                canvas.drawBitmap(textures.lookoutBitmap, null, dstRect, crispPaint)
                if (p > 0.005f) {
                    overlayPaint.alpha = (p * 255f).toInt().coerceIn(0, 255)
                    canvas.drawBitmap(textures.midTurnBitmap, null, dstRect, overlayPaint)
                }
            }
            t < 0.998f -> {
                // Stage 2: 45° Oblique Mid-Turn -> 90° Full Eye Contact (pixel-locked body & skyline!)
                val p = smoothStep(0.04f, 0.96f, (t - 0.50f) / 0.50f)
                canvas.drawBitmap(textures.midTurnBitmap, null, dstRect, crispPaint)
                if (p > 0.005f) {
                    overlayPaint.alpha = (p * 255f).toInt().coerceIn(0, 255)
                    canvas.drawBitmap(textures.turnedBitmap, null, dstRect, overlayPaint)
                }
            }
            else -> {
                canvas.drawBitmap(textures.turnedBitmap, null, dstRect, crispPaint)
            }
        }

        // ── PASS 2: REAL PHOTOREALISTIC WIND-SWEPT HAIR HALO (ONLY ON GUSTS, ZERO FACE/SKYLINE WARP!) ──
        val windPatch = textures.windHairPatch
        if (windPatch != null && !windPatch.bitmap.isRecycled && t > 0.85f && safeWindGust > 0.28f) {
            val gustWeight = smoothStep(0.28f, 0.94f, safeWindGust) * smoothStep(0.85f, 0.98f, t)
            if (gustWeight > 0.02f) {
                patchRect.set(
                    originX + windPatch.normLeft * panW,
                    originY + windPatch.normTop * panH,
                    originX + windPatch.normRight * panW,
                    originY + windPatch.normBottom * panH
                )
                overlayPaint.alpha = (gustWeight * 228f).toInt().coerceIn(0, 255)
                canvas.drawBitmap(windPatch.bitmap, null, patchRect, overlayPaint)
            }
        }

        // Engage eye-contact facial patches when facing camera (always active for single-angle master frames)
        val faceVisibility = if (textures.lookoutBitmap === textures.turnedBitmap) 1f else smoothStep(0.86f, 0.99f, t)
        if (faceVisibility <= 0.01f) {
            overlayPaint.alpha = 255
            return
        }

        // ── PASS 3: REAL PHOTOREALISTIC EYELID BLINK (100% SYNCHRONIZED BOTH EYES!) ──
        drawRealEyelidBlinkPatch(
            canvas = canvas,
            patch = textures.leftEyeClosedPatch,
            originX = originX,
            originY = originY,
            panW = panW,
            panH = panH,
            blink = safeBlink,
            visibility = faceVisibility
        )
        drawRealEyelidBlinkPatch(
            canvas = canvas,
            patch = textures.rightEyeClosedPatch,
            originX = originX,
            originY = originY,
            panW = panW,
            panH = panH,
            blink = safeBlink,
            visibility = faceVisibility
        )

        // ── PASS 4: REAL PHOTOREALISTIC SPEAKING MOUTH & JAW (TURKISH VISEME + AUDIO RMS!) ──
        drawRealSpeakingMouthPatch(
            canvas = canvas,
            patch = textures.mouthOpenPatch,
            originX = originX,
            originY = originY,
            panW = panW,
            panH = panH,
            viseme = params.viseme,
            visibility = faceVisibility
        )

        overlayPaint.alpha = 255
    }

    /**
     * Reveals the real photographed closed eyelid from top to bottom as [blink] rises 0 -> 1,
     * mimicking true upper-eyelid descent over the iris with real skin texture and eyelashes.
     */
    private fun drawRealEyelidBlinkPatch(
        canvas: AndroidCanvas,
        patch: FeatheredPhotoPatch?,
        originX: Float,
        originY: Float,
        panW: Float,
        panH: Float,
        blink: Float,
        visibility: Float
    ) {
        if (patch == null || patch.bitmap.isRecycled || blink <= 0.02f) return
        val left = originX + patch.normLeft * panW
        val top = originY + patch.normTop * panH
        val right = originX + patch.normRight * panW
        val bottom = originY + patch.normBottom * panH
        val height = bottom - top

        // Smooth top-down eyelid curtain descent + crisp opacity so full closure is 100% opaque real skin
        val alpha = (smoothStep(0.02f, 0.55f, blink) * visibility * 255f).toInt().coerceIn(0, 255)
        if (alpha <= 1) return

        val revealFraction = (0.34f + 0.66f * blink).coerceIn(0.34f, 1.0f)
        patchRect.set(left, top, right, bottom)
        clipRect.set(left, top, right, top + height * revealFraction)

        canvas.save()
        canvas.clipRect(clipRect)
        overlayPaint.alpha = alpha
        canvas.drawBitmap(patch.bitmap, null, patchRect, overlayPaint)
        canvas.restore()
    }

    /**
     * Composites the real photographed speaking mouth + jaw patch synced to Turkish phonemes & audio RMS.
     * Scales gently around the upper-lip pivot so conversational vowels open smoothly without double-lip
     * ghosting or stretched chin artifacts.
     */
    private fun drawRealSpeakingMouthPatch(
        canvas: AndroidCanvas,
        patch: FeatheredPhotoPatch?,
        originX: Float,
        originY: Float,
        panW: Float,
        panH: Float,
        viseme: FacialVisemeFrame,
        visibility: Float
    ) {
        val jaw = safeFiniteFloat(viseme.jawDrop, 0f).coerceIn(0f, 1f)
        if (patch == null || patch.bitmap.isRecycled || jaw <= 0.04f) return

        val left = originX + patch.normLeft * panW
        val top = originY + patch.normTop * panH
        val right = originX + patch.normRight * panW
        val bottom = originY + patch.normBottom * panH
        val w = right - left
        val h = bottom - top

        // Crisp alpha curve: reaches full opacity on normal open vowels so real lips & teeth look sharp,
        // and fades cleanly to closed lips on M/B/P bilabials & pauses between words!
        val alpha = (smoothStep(0.04f, 0.44f, jaw) * visibility * 255f).toInt().coerceIn(0, 255)
        if (alpha <= 1) return

        // Anchor gentle anatomical articulation at the upper-lip / philtrum line so the nose stays locked
        val pivotX = left + w * 0.50f
        val pivotY = top + h * 0.24f
        val scaleY = (0.88f + jaw * 0.16f).coerceIn(0.88f, 1.05f)
        val scaleX = (1.00f + safeFiniteFloat(viseme.lipWidth, 0f) * 0.16f).coerceIn(0.94f, 1.06f)

        patchRect.set(left, top, right, bottom)
        canvas.save()
        canvas.scale(scaleX, scaleY, pivotX, pivotY)
        overlayPaint.alpha = alpha
        canvas.drawBitmap(patch.bitmap, null, patchRect, overlayPaint)
        canvas.restore()
    }

    private fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val safeX = safeFiniteFloat(x, edge0)
        val t = ((safeX - edge0) / max(edge1 - edge0, 0.0001f)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }
}
