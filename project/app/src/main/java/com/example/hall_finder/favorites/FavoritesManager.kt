package com.example.hall_finder.favorites

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

object FavoritesManager {

    private const val PREFS_NAME = "hall_finder_favorites"
    private const val KEY_FAVORITES = "favorite_node_ids"

    fun getFavorites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun addFavorite(context: Context, nodeId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getFavorites(context).toMutableSet()
        current.add(nodeId)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun removeFavorite(context: Context, nodeId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getFavorites(context).toMutableSet()
        current.remove(nodeId)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun isFavorite(context: Context, nodeId: String): Boolean {
        return getFavorites(context).contains(nodeId)
    }

    fun toggleFavorite(context: Context, nodeId: String): Boolean {
        return if (isFavorite(context, nodeId)) {
            removeFavorite(context, nodeId)
            false
        } else {
            addFavorite(context, nodeId)
            true
        }
    }
}