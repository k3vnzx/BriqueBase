package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeneratedAdResult(
    val title: String,
    val description: String,
    val whatsappVersion: String,
    val instagramVersion: String,
    val marketplaceVersion: String,
    val highlights: List<String>,
    val callToAction: String,
    val hashtags: String,
    val isFromGeminiAi: Boolean,
    val notice: String? = null
)

class GeminiAdService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateAd(
        productName: String,
        brand: String,
        model: String,
        condition: String,
        price: Double,
        defects: String,
        accessories: String,
        platform: String,
        tone: String,
        targetAudience: String = "Geral"
    ): GeneratedAdResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() &&
                !apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
                !apiKey.contains("PLACEHOLDER", ignoreCase = true)

        if (hasValidKey) {
            try {
                return@withContext callGeminiApi(
                    apiKey = apiKey,
                    productName = productName,
                    brand = brand,
                    model = model,
                    condition = condition,
                    price = price,
                    defects = defects,
                    accessories = accessories,
                    platform = platform,
                    tone = tone,
                    targetAudience = targetAudience
                )
            } catch (e: Exception) {
                Log.w("GeminiAdService", "Gemini API call failed, falling back to local template engine: ${e.message}")
                val localResult = generateLocalCopy(
                    productName, brand, model, condition, price, defects, accessories, platform, tone
                )
                return@withContext localResult.copy(
                    notice = "Modo Offline ativo (Falha na conexão com Gemini API: ${e.message?.take(50)}...). Anúncio gerado com base em copywriting profissional de revenda."
                )
            }
        } else {
            val localResult = generateLocalCopy(
                productName, brand, model, condition, price, defects, accessories, platform, tone
            )
            return@withContext localResult.copy(
                notice = "Modo Copywriting Estruturado. Para habilitar IA Gemini em tempo real, configure sua GEMINI_API_KEY no painel Secrets."
            )
        }
    }

    private fun callGeminiApi(
        apiKey: String,
        productName: String,
        brand: String,
        model: String,
        condition: String,
        price: Double,
        defects: String,
        accessories: String,
        platform: String,
        tone: String,
        targetAudience: String
    ): GeneratedAdResult {
        val prompt = """
            Você é um especialista sênior em copywriting de vendas e revenda de smartphones/eletrônicos no Brasil.
            Crie anúncios de alta conversão para o seguinte produto:
            - Produto: $productName
            - Marca/Modelo: $brand $model
            - Condição/Estado: $condition
            - Preço de Venda: R$ ${"%.2f".format(price)}
            - Defeitos ou Detalhes: ${if (defects.isBlank()) "Nenhum defeito, 100% revisado e testado" else defects}
            - Acessórios Incluídos: ${if (accessories.isBlank()) "Apenas aparelho" else accessories}
            - Plataforma Alvo: $platform
            - Tom de Voz: $tone
            - Público-Alvo: $targetAudience

            Regras estritas:
            1. NÃO invente garantia, acessórios ou especificações que não foram informadas.
            2. Seja honesto e transparente sobre o estado e detalhes.
            3. Responda ESTRITAMENTE em formato JSON com o seguinte formato, sem formatação markdown em volta:
            {
              "title": "título chamativo e profissional com menos de 70 caracteres",
              "description": "descrição completa e persuasiva",
              "whatsappVersion": "texto formatado com emojis e quebras de linha para enviar no WhatsApp",
              "instagramVersion": "texto engajador para feed/story do Instagram",
              "marketplaceVersion": "texto direto com especificações técnicas para o Facebook Marketplace / OLX",
              "highlights": ["destaque 1", "destaque 2", "destaque 3", "destaque 4"],
              "callToAction": "frase de ação irresistível para fechar hoje",
              "hashtags": "#iphone #apple #seminovos #brique"
            }
        """.trimIndent()

        val jsonPayload = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            }
            put("generationConfig", generationConfig)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Resposta vazia da API")
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: throw Exception("Nenhum texto retornado")

            // Parse response JSON
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adJson = JSONObject(cleanJson)
            val highlightsArray = adJson.optJSONArray("highlights")
            val highlightsList = mutableListOf<String>()
            if (highlightsArray != null) {
                for (i in 0 until highlightsArray.length()) {
                    highlightsList.add(highlightsArray.getString(i))
                }
            }

            return GeneratedAdResult(
                title = adJson.optString("title", "$productName $condition - R$ ${"%.2f".format(price)}"),
                description = adJson.optString("description", ""),
                whatsappVersion = adJson.optString("whatsappVersion", ""),
                instagramVersion = adJson.optString("instagramVersion", ""),
                marketplaceVersion = adJson.optString("marketplaceVersion", ""),
                highlights = if (highlightsList.isNotEmpty()) highlightsList else listOf("Pronta Entrega", "Testado e Revisado", "Ótimo Preço"),
                callToAction = adJson.optString("callToAction", "Chame no WhatsApp e garanta antes que venda!"),
                hashtags = adJson.optString("hashtags", "#revenda #eletronicos #oportunidade"),
                isFromGeminiAi = true,
                notice = "Gerado via Gemini 3.5 Flash com IA ao vivo."
            )
        }
    }

    fun generateLocalCopy(
        productName: String,
        brand: String,
        model: String,
        condition: String,
        price: Double,
        defects: String,
        accessories: String,
        platform: String,
        tone: String
    ): GeneratedAdResult {
        val formattedPrice = "R$ ${"%.2f".format(price)}"
        val cleanAccessories = if (accessories.isBlank()) "Apenas aparelho" else accessories
        val cleanDefects = if (defects.isBlank()) "Aparelho 100% funcional e revisado, sem defeitos." else defects

        val (title, hook) = when (tone) {
            "Direto" -> Pair(
                "$productName - $condition - $formattedPrice à vista",
                "OPORTUNIDADE DIRETA! Preço justo para sair rápido."
            )
            "Premium" -> Pair(
                "★ $productName impecável ($condition) - Garantia e Procedência",
                "Qualidade e procedência garantida para compradores exigentes."
            )
            "Focado em Conversão" -> Pair(
                "🔥 QUEIMA! $productName por apenas $formattedPrice - Retirada Hoje",
                "Oportunidade única abaixo da tabela! Quem chegar primeiro leva."
            )
            "Informal" -> Pair(
                "Brique top! $productName topíssimo, só pegar e usar",
                "Fala galera! To passando esse aparelho em excelente estado."
            )
            else -> Pair(
                "$productName ($condition) - $formattedPrice - Revisado e Pronto",
                "Excelente oportunidade de compra com total transparência e segurança."
            )
        }

        val highlights = listOf(
            "Condição: $condition",
            "Preço imperdível: $formattedPrice",
            "Acessórios: $cleanAccessories",
            "Testes: 100% testado e funcional"
        )

        val whatsapp = """
            📱 *$productName* ($condition)
            💰 Valor especial: *$formattedPrice*
            
            ✅ *Destaques do aparelho:*
            • Estado: $condition
            • Acessórios: $cleanAccessories
            • Detalhes/Condição: $cleanDefects
            
            ⚡ Testado na hora da entrega com total transparência!
            📍 Retirada em local público e seguro / Envio a combinar.
            
            💬 _Chama no chat ou direct para fechar agora!_
        """.trimIndent()

        val instagram = """
            ✨ DISPONÍVEL NO ESTOQUE!
            
            $productName ($condition)
            Por apenas $formattedPrice!
            
            🔍 $hook
            📦 Inclui: $cleanAccessories
            🛡️ $cleanDefects
            
            📲 Mande uma mensagem na DM para reservar ou agendar a retirada!
            
            #${brand.lowercase().replace(" ", "")} #${model.lowercase().replace(" ", "")} #brique #revenda #seminovos #oportunidade #eletronicos
        """.trimIndent()

        val marketplace = """
            $title
            
            Valor: $formattedPrice
            Estado: $condition
            Marca/Modelo: $brand $model
            
            ITENS INCLUSOS:
            - $cleanAccessories
            
            DETALHES IMPORTANTES:
            - $cleanDefects
            - Testamos tudo na hora da compra (câmeras, bateria, tela, conexões).
            - Pagamento via Pix ou Dinheiro. Cartão de crédito sujeito a taxa da máquina.
            
            Local de entrega a combinar em ponto seguro ou shopping.
            Disponível enquanto o anúncio estiver ativo!
        """.trimIndent()

        return GeneratedAdResult(
            title = title,
            description = marketplace,
            whatsappVersion = whatsapp,
            instagramVersion = instagram,
            marketplaceVersion = marketplace,
            highlights = highlights,
            callToAction = "Interessou? Mande mensagem agora para fechar negócio!",
            hashtags = "#${brand.lowercase().replace(" ", "")} #revenda #seminovos #briquei",
            isFromGeminiAi = false
        )
    }
}
