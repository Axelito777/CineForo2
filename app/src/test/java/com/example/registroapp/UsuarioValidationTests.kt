package com.example.registroapp
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse


class UsuarioValidator {
    fun esEmailValido(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun esContraseñaValida(pass: String): Boolean {
        return pass.length >= 6
    }

    fun contraseñasCoinciden(pass1: String, pass2: String): Boolean {
        return pass1 == pass2
    }

    fun esNombreValido(nombre: String): Boolean {
        return nombre.isNotEmpty() && nombre.length >= 3
    }

    fun esGeneroValido(genero: String, generos: List<String>): Boolean {
        return generos.contains(genero)
    }
}

class UsuarioValidationTests {
    private lateinit var validator: UsuarioValidator

    @Before
    fun setup() {
        validator = UsuarioValidator()
    }

    @Test
    fun testEmailValido() {
        val email = "usuario@gmail.com"
        assertTrue(validator.esEmailValido(email))
    }

    @Test
    fun testEmailInvalido() {
        val email = "usuariogmail"
        assertFalse(validator.esEmailValido(email))
    }

    @Test
    fun testContraseñaMinima6Caracteres() {
        val pass = "123456"
        assertTrue(validator.esContraseñaValida(pass))
    }

    @Test
    fun testContraseñaMenor6Caracteres() {
        val pass = "12345"
        assertFalse(validator.esContraseñaValida(pass))
    }

    @Test
    fun testContraseñasCoinciden() {
        val pass1 = "Password123"
        val pass2 = "Password123"
        assertTrue(validator.contraseñasCoinciden(pass1, pass2))
    }

    @Test
    fun testContraseñasNoCoinciden() {
        val pass1 = "Password123"
        val pass2 = "Password456"
        assertFalse(validator.contraseñasCoinciden(pass1, pass2))
    }

    @Test
    fun testNombreNoVacio() {
        val nombre = "Juan Pérez"
        assertTrue(validator.esNombreValido(nombre))
    }

    @Test
    fun testNombreVacio() {
        val nombre = ""
        assertFalse(validator.esNombreValido(nombre))
    }

    @Test
    fun testGeneroValido() {
        val generos = listOf("Acción", "Comedia", "Drama")
        assertTrue(validator.esGeneroValido("Acción", generos))
    }

    @Test
    fun testGeneroInvalido() {
        val generos = listOf("Acción", "Comedia", "Drama")
        assertFalse(validator.esGeneroValido("Ficción", generos))
    }
}
