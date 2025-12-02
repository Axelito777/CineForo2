package com.example.registroapp

import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ForoValidator {
    fun esTemaValido(titulo: String, descripcion: String): Boolean {
        return titulo.isNotEmpty() &&
                descripcion.isNotEmpty() &&
                titulo.length <= 200 &&
                descripcion.length <= 2000
    }

    fun esCategoriaValida(categoria: String, categorias: List<String>): Boolean {
        return categorias.contains(categoria)
    }
}

class ForoValidationTests {

    private lateinit var validator: ForoValidator

    @Before
    fun setup() {
        validator = ForoValidator()
    }

    @Test
    fun testTemaValido() {
        val titulo = "Mejor película del 2024"
        val descripcion = "Discusión sobre las mejores películas del año"
        assertTrue(validator.esTemaValido(titulo, descripcion))
    }

    @Test
    fun testTemaConTituloVacio() {
        val titulo = ""
        val descripcion = "Descripción válida"
        assertFalse(validator.esTemaValido(titulo, descripcion))
    }

    @Test
    fun testTemaConDescripcionVacia() {
        val titulo = "Título válido"
        val descripcion = ""
        assertFalse(validator.esTemaValido(titulo, descripcion))
    }

    @Test
    fun testTituloMayorMaximo() {
        val titulo = "a".repeat(201)
        val descripcion = "Descripción válida"
        assertFalse(validator.esTemaValido(titulo, descripcion))
    }

    @Test
    fun testCategoriaValida() {
        val categorias = listOf("General", "Películas", "Series")
        assertTrue(validator.esCategoriaValida("General", categorias))
    }

    @Test
    fun testCategoriaInvalida() {
        val categorias = listOf("General", "Películas", "Series")
        assertFalse(validator.esCategoriaValida("Deportes", categorias))
    }

    @Test
    fun testDescripcionMayorMaximo() {
        val titulo = "Título válido"
        val descripcion = "a".repeat(2001)
        assertFalse(validator.esTemaValido(titulo, descripcion))
    }
}