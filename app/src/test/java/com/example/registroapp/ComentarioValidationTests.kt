package com.example.registroapp

import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
class ComentarioValidator {
    fun esComentarioValido(contenido: String): Boolean {
        return contenido.isNotEmpty() &&
                contenido.length >= 5 &&
                contenido.length <= 500
    }
}

class ComentarioValidationTests {

    private lateinit var validator: ComentarioValidator

    @Before
    fun setup() {
        validator = ComentarioValidator()
    }

    @Test
    fun testComentarioNoVacio() {
        val contenido = "Esta película es excelente"
        assertTrue(validator.esComentarioValido(contenido))
    }

    @Test
    fun testComentarioVacio() {
        val contenido = ""
        assertFalse(validator.esComentarioValido(contenido))
    }

    @Test
    fun testComentarioMenor5Caracteres() {
        val contenido = "Bien"
        assertFalse(validator.esComentarioValido(contenido))
    }

    @Test
    fun testComentarioMayor500Caracteres() {
        val contenido = "a".repeat(501)
        assertFalse(validator.esComentarioValido(contenido))
    }

    @Test
    fun testLongitudComentarioValida() {
        val contenido = "a".repeat(100)
        assertTrue(validator.esComentarioValido(contenido))
    }
}