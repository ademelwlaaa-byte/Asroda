package com.example.util

/**
 * Turkish diacritic-tolerant search filter for saved Story Bookmarks.
 */
object StoryBookmarkSearchHelper {

    fun filter(
        bookmarks: List<StoryBookmarkStore.Bookmark>,
        rawQuery: String
    ): List<StoryBookmarkStore.Bookmark> {
        val q = ChatSearchHelper.foldTurkish(rawQuery.trim())
        if (q.isEmpty()) return bookmarks
        return bookmarks.filter { bm ->
            ChatSearchHelper.foldTurkish(bm.title).contains(q) ||
                ChatSearchHelper.foldTurkish(bm.snippet).contains(q)
        }
    }
}
