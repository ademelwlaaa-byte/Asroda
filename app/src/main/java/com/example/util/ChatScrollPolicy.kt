package com.example.util

/**
 * Pure, deterministic scroll-following policy for [com.example.ui.screens.ChatScreen].
 *
 * Solves two real-world streaming UX issues:
 * 1. Tall multi-paragraph streaming bubbles stalling once their bottom grows >80 px past the
 *    viewport, or snapping back to the top of the bubble (`scrollToItem(lastIndex, 0)`).
 * 2. Auto-scroll fighting users who intentionally scrolled up to read earlier messages while
 *    a reply is still streaming or finishing.
 */
object ChatScrollPolicy {

    const val DEFAULT_BOTTOM_TOLERANCE_PX: Int = 80
    const val STREAM_SCROLL_THROTTLE_MS: Long = 120L

    /**
     * Returns true when the list is positioned at or within [tolerancePx] of the true bottom.
     */
    fun isAtBottom(
        totalItems: Int,
        canScrollForward: Boolean,
        lastVisibleIndex: Int?,
        lastVisibleEndPx: Int?,
        viewportEndPx: Int,
        tolerancePx: Int = DEFAULT_BOTTOM_TOLERANCE_PX
    ): Boolean {
        if (totalItems <= 0 || !canScrollForward) return true
        val idx = lastVisibleIndex ?: return false
        val endPx = lastVisibleEndPx ?: return false
        if (idx < 0 || idx > totalItems - 1) return false
        val itemsFromEnd = (totalItems - 1) - idx
        if (itemsFromEnd > 0) return false
        val safeTolerance = tolerancePx.coerceAtLeast(0).toLong()
        return (endPx.toLong() - viewportEndPx.toLong()) <= safeTolerance
    }

    /**
     * Updates the sticky-bottom flag based on user scroll gesture vs. bottom proximity.
     *
     * - Sending a new message or tapping the scroll-to-bottom FAB always re-engages sticky mode.
     * - Reaching the bottom naturally re-engages sticky mode.
     * - Actively scrolling upward away from the bottom disengages sticky mode so the UI never
     *   fights the user's finger.
     * - When not actively scrolling (e.g., streaming text expanding the last bubble), sticky mode
     *   is preserved even if the bubble temporarily overflows past the bottom tolerance.
     */
    fun nextStickyState(
        currentSticky: Boolean,
        isUserScrollInProgress: Boolean,
        isAtBottom: Boolean,
        userInitiatedSnapToBottom: Boolean = false
    ): Boolean {
        if (userInitiatedSnapToBottom) return true
        if (isAtBottom) return true
        if (isUserScrollInProgress) return false
        return currentSticky
    }

    /**
     * Computes how many pixels to scroll forward to reveal the bottom of the last item, or `0f`
     * if already fully visible. Returns `null` when the last item is not currently composed in
     * the viewport (caller should fall back to `scrollToItem(totalItems - 1)`).
     */
    fun computeBottomOverflowPx(
        totalItems: Int,
        lastVisibleIndex: Int?,
        lastVisibleEndPx: Int?,
        viewportEndPx: Int
    ): Float? {
        if (totalItems <= 0) return 0f
        val idx = lastVisibleIndex ?: return null
        val endPx = lastVisibleEndPx ?: return null
        if (idx < 0 || idx < totalItems - 1) return null
        val overflow = endPx.toLong() - viewportEndPx.toLong()
        return if (overflow > 0L) overflow.toFloat() else 0f
    }

    /**
     * Determines whether an automatic scroll to bottom should fire when streaming chunks arrive.
     */
    fun shouldAutoScrollOnStreamChunk(
        chunk: String?,
        isStickyToBottom: Boolean,
        isUserScrollInProgress: Boolean,
        nowMs: Long,
        lastScrollMs: Long,
        throttleMs: Long = STREAM_SCROLL_THROTTLE_MS
    ): Boolean {
        if (chunk.isNullOrEmpty()) return false
        if (!isStickyToBottom || isUserScrollInProgress) return false
        if (nowMs < lastScrollMs) return true // Backward wall-clock adjustment must never freeze scrolling
        val safeThrottle = throttleMs.coerceAtLeast(0L)
        return (nowMs - lastScrollMs) >= safeThrottle
    }
}
