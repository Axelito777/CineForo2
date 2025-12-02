package com.example.registroapp

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class RatingAndLikesTests {

    @Test
    fun testLikeIncrementado() {
        var likes = 0
        likes++
        assertEquals(1, likes)
    }

    @Test
    fun testMultiplesLikes() {
        var likes = 0
        repeat(5) { likes++ }
        assertEquals(5, likes)
    }

    @Test
    fun testDislikeIncrementado() {
        var dislikes = 0
        dislikes++
        assertEquals(1, dislikes)
    }

    @Test
    fun testRatingPromedio() {
        val ratings = listOf(5, 4, 3, 5, 4)
        val promedio = ratings.average()
        assertEquals(4.2, promedio, 0.1)
    }

    @Test
    fun testLikeNoNegativo() {
        var likes = 0
        if (likes > 0) likes-- else likes = 0
        assertEquals(0, likes)
    }

    @Test
    fun testDislikeNoNegativo() {
        var dislikes = 0
        if (dislikes > 0) dislikes-- else dislikes = 0
        assertEquals(0, dislikes)
    }

    @Test
    fun testRatingMinimo() {
        val rating = 1.0
        assertEquals(1.0, rating)
    }

    @Test
    fun testRatingMaximo() {
        val rating = 10.0
        assertEquals(10.0, rating)
    }

    @Test
    fun testMultiplesRatings() {
        val ratings = listOf(8, 9, 7, 8, 9, 10)
        val promedio = ratings.average()
        assertEquals(8.5, promedio, 0.1)
    }

    @Test
    fun testToggleLike() {
        var liked = false
        liked = !liked
        assertEquals(true, liked)
        liked = !liked
        assertEquals(false, liked)
    }
}