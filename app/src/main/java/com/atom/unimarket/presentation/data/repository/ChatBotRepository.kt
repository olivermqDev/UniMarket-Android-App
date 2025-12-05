package com.atom.unimarket.presentation.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.firestore.FirebaseFirestore
import com.atom.unimarket.presentation.data.Product
import kotlinx.coroutines.tasks.await
import com.atom.unimarket.BuildConfig





class ChatbotRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Configurar el modelo generativo con la API Key
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash", // un modelo válido
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Obtener respuesta del chatbot
    suspend fun getChatbotResponse(userMessage: String): Result<String> {
        return try {
            val productsContext = getProductsContext()

            val prompt = """
                Eres un asistente virtual de UniMarket, un marketplace universitario.
                
                Contexto de productos disponibles:
                $productsContext
                
                Pregunta del usuario: $userMessage
                
                Responde de manera amigable y útil, proporcionando información sobre:
                - Productos disponibles y sus características
                - Precios y categorías
                - Disponibilidad
                - Recomendaciones personalizadas
                
                Si no tienes información específica, sugiere al usuario buscar en el catálogo o contactar directamente con los vendedores.
            """.trimIndent()

            val response = try {
                generativeModel.generateContent(prompt)
            } catch (e: Exception) {
                return Result.failure(Exception("Error generando contenido con el modelo '${generativeModel.modelName}': ${e.message}", e))
            }

            Result.success(response.text ?: "Lo siento, no pude generar una respuesta.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener contexto de productos
    private suspend fun getProductsContext(): String {
        return try {
            val snapshot = firestore.collection("products")
                .limit(20)
                .get()
                .await()

            val products = snapshot.documents.mapNotNull {
                it.toObject(Product::class.java)
            }

            if (products.isEmpty()) {
                "No hay productos disponibles actualmente."
            } else {
                products.joinToString("\n") { product ->
                    "- ${product.name} (${product.category}): $${product.price} - ${product.description}"
                }
            }
        } catch (e: Exception) {
            "No se pudo cargar la información de productos."
        }
    }

    // Buscar productos relacionados
    suspend fun getRelevantProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.documents.mapNotNull {
                it.toObject(Product::class.java)
            }.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true) ||
                        product.category.contains(query, ignoreCase = true)
            }.take(5)

            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}