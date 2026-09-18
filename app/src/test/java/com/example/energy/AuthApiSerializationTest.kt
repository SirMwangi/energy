package com.example.energy

import com.example.energy.data.remote.RetrofitClient
import com.example.energy.data.remote.api.AuthResponse
import com.example.energy.data.remote.api.LoginRequest
import com.example.energy.data.remote.api.RegisterRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthApiSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun registerRequest_serializes_to_exact_backend_property_names() {
        val request = RegisterRequest(
            name = "John Doe",
            email = "john.doe@example.com",
            phoneNumber = "+254712345678",
            password = "SecurePassword123!"
        )

        val jsonString = json.encodeToString(request)
        val jsonObject = json.decodeFromString<JsonObject>(jsonString)

        // Verify exact property names matching backend RegisterRequest / User entity:
        // - name
        // - email
        // - phoneNumber (camelCase)
        // - password
        assertTrue(jsonObject.containsKey("name"))
        assertTrue(jsonObject.containsKey("email"))
        assertTrue(jsonObject.containsKey("phoneNumber"))
        assertTrue(jsonObject.containsKey("password"))

        assertEquals("John Doe", jsonObject["name"]?.jsonPrimitive?.content)
        assertEquals("john.doe@example.com", jsonObject["email"]?.jsonPrimitive?.content)
        assertEquals("+254712345678", jsonObject["phoneNumber"]?.jsonPrimitive?.content)
        assertEquals("SecurePassword123!", jsonObject["password"]?.jsonPrimitive?.content)
    }

    @Test
    fun loginRequest_serializes_to_exact_property_names() {
        val request = LoginRequest(
            email = "john.doe@example.com",
            password = "SecurePassword123!"
        )

        val jsonString = json.encodeToString(request)
        val jsonObject = json.decodeFromString<JsonObject>(jsonString)

        assertTrue(jsonObject.containsKey("email"))
        assertTrue(jsonObject.containsKey("password"))
        assertEquals("john.doe@example.com", jsonObject["email"]?.jsonPrimitive?.content)
        assertEquals("SecurePassword123!", jsonObject["password"]?.jsonPrimitive?.content)
    }

    @Test
    fun authResponse_deserializes_from_backend_user_or_token_payload() {
        val rawJson = """
            {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "name": "John Doe",
                "email": "john.doe@example.com",
                "phoneNumber": "+254712345678",
                "token": "mock-jwt-token-12345",
                "tokenType": "Bearer",
                "message": "Registration successful"
            }
        """.trimIndent()

        val response = json.decodeFromString<AuthResponse>(rawJson)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.id)
        assertEquals("John Doe", response.name)
        assertEquals("john.doe@example.com", response.email)
        assertEquals("+254712345678", response.phoneNumber)
        assertEquals("mock-jwt-token-12345", response.token)
        assertEquals("Bearer", response.tokenType)
        assertEquals("Registration successful", response.message)
    }

    @Test
    fun retrofitClient_initializes_and_exposes_authApi() {
        assertNotNull(RetrofitClient.authApi)
    }
}
