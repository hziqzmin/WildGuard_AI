package io.github.hziqzmin.wildguardai

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileOutputStream

data class ChatMessage(
    val id: Int,
    val text: String,
    val author: Author
)

enum class Author { USER, AI }

data class KnowledgeChunk(
    val topic: String? = null,
    val region: String? = null,
    val knot_name: String? = null,
    val description: String? = null,
    val use_cases: List<String>? = null,
    val instructions: String? = null,
    val text: String? = null,
    val embedding: List<Double>
)

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    companion object {
        private const val TAG = "WildGuardAI"

        // Gemma task file on *device*
        private const val MODEL_PATH = "/data/local/tmp/llm/gemma3-1B-it-int4.task"

        // Embedding dimension for USE-QA on-device
        private const val EMBEDDING_DIM = 512

        // How many passages to retrieve for context
        private const val TOP_K = 3
    }

    // Embedding model (TFLite) for similarity search
    private var finder: Interpreter? = null

    // LLM for generation
    private var brain: LlmInference? = null

    // Knowledge base with pre-computed embeddings
    private var library: List<KnowledgeChunk> = emptyList()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isAITyping = MutableStateFlow(false)
    val isAITyping = _isAITyping.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initializeAI()

            val initialText = buildString {
                if (brain != null) {
                    append("Hi! I'm WildGuard AI. How can I help you?\n")
                } else {
                    append("AI model is not loaded or failed to respond.\n")
                }

                if (finder == null) {
                    append("⚠️ Embedding model not loaded, using keyword-only retrieval.\n")
                }

                if (library.isEmpty()) {
                    append("⚠️ Knowledge base is empty or failed to load.\n")
                }
            }.trim()

            _messages.value = listOf(
                ChatMessage(
                    id = 0,
                    text = initialText,
                    author = Author.AI
                )
            )

            _isLoading.value = false
        }
    }

    private fun initializeAI() {
        Log.d(TAG, "Initializing AI...")

        // 1) Try to load embedder (TFLite) – but don't fail everything if this breaks
        try {
            val finderModelFile = copyAssetToCache("embedding_model.tflite")
            finder = Interpreter(finderModelFile)
            Log.d(TAG, "Finder (embedder) loaded from: ${finderModelFile.absolutePath}")
        } catch (e: Exception) {
            finder = null
            Log.e(TAG, "Failed to load embedding model (finder). Will fallback to keyword search.", e)
        }

        // 2) Try to load knowledge base – LLM can still work without it
        try {
            val gson = Gson()
            val jsonStream = app.assets.open("knowledge_base.json").bufferedReader()
            val type = object : TypeToken<List<KnowledgeChunk>>() {}.type
            library = gson.fromJson(jsonStream, type)
            Log.d(TAG, "Knowledge base loaded with ${library.size} chunks")
        } catch (e: Exception) {
            library = emptyList()
            Log.e(TAG, "Failed to load knowledge_base.json. RAG will be disabled.", e)
        }

        // 3) Load Gemma LLM – this is the critical part
        try {
            Log.d(TAG, "Loading Gemma model from: $MODEL_PATH")

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(MODEL_PATH)
                // keep prompts small; Gemma-3 1B on-device has ~512 token limit
                .setMaxTokens(256)
                .build()

            brain = LlmInference.createFromOptions(app, options)
            Log.d(TAG, "Gemma loaded: ${brain != null}")
        } catch (e: Exception) {
            brain = null
            Log.e(TAG, "Failed to load Gemma LLM", e)
        }
    }

    fun sendMessage(userText: String) {
        if (_isAITyping.value) return

        _messages.value += ChatMessage(messages.value.size, userText, Author.USER)

        if (brain == null) {
            _messages.value += ChatMessage(
                id = messages.value.size,
                text = "AI model is not loaded. Check that the Gemma .task file exists at:\n$MODEL_PATH",
                author = Author.AI
            )
            return
        }

        _isAITyping.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1) Retrieve relevant knowledge
                val topChunks: List<KnowledgeChunk> =
                    if (finder != null && library.isNotEmpty()) {
                        try {
                            val q = embedText(userText)
                            findTopMatches(q, library, TOP_K)
                        } catch (e: Exception) {
                            Log.e(TAG, "Embedding failed, falling back to keyword search", e)
                            findTopMatchesByKeyword(userText, library, TOP_K)
                        }
                    } else {
                        findTopMatchesByKeyword(userText, library, TOP_K)
                    }

                val context = if (topChunks.isEmpty()) {
                    "No specific survival notes were found. Answer based on your general knowledge but stay brief and practical."
                } else {
                    topChunks.joinToString("\n\n") { chunk ->
                        "- ${chunk.asContextText()}"
                    }
                }

                // 2) Build compact RAG prompt
                val prompt = """
                    <start_of_turn>user
                    You are WildGuard AI, an offline wilderness survival assistant.
                    Use ONLY the information below when possible. If something is missing,
                    say you are not sure instead of making things up.

                    Survival notes:
                    $context

                    User question:
                    $userText

                    Give a clear, step-by-step answer tailored for real backcountry situations.<end_of_turn>
                    <start_of_turn>model
                """.trimIndent()

                // 3) Generate
                Log.d(TAG, "Calling generateResponse()")
                val result = brain!!.generateResponse(prompt)
                Log.d(TAG, "LLM returned: ${result.take(120)}...")

                _messages.value += ChatMessage(
                    id = messages.value.size,
                    text = result.trim(),
                    author = Author.AI
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error during sendMessage()", e)
                _messages.value += ChatMessage(
                    id = messages.value.size,
                    text = "Error: ${e.message}",
                    author = Author.AI
                )
            } finally {
                _isAITyping.value = false
            }
        }
    }

    /** Run the TFLite embedding model on a single piece of text. */
    private fun embedText(text: String): FloatArray {
        val model = finder ?: error("Embedding model (finder) not initialized")

        val input = arrayOf(text)
        val output = Array(1) { FloatArray(EMBEDDING_DIM) }

        model.run(input, output)
        return output[0]
    }

    /** Cosine-similarity retrieval using precomputed embeddings in the KB. */
    private fun findTopMatches(
        q: FloatArray,
        kb: List<KnowledgeChunk>,
        k: Int
    ): List<KnowledgeChunk> {
        if (kb.isEmpty()) return emptyList()

        val scored = kb.map { chunk ->
            val vec = chunk.embedding.map(Double::toFloat).toFloatArray()
            chunk to cosine(q, vec)
        }

        return scored
            .sortedByDescending { it.second }
            .take(k)
            .map { it.first }
    }

    /** Fallback retrieval: simple keyword matching over text fields. */
    private fun findTopMatchesByKeyword(
        query: String,
        kb: List<KnowledgeChunk>,
        k: Int
    ): List<KnowledgeChunk> {
        if (kb.isEmpty()) return emptyList()

        val queryWords = query
            .lowercase()
            .split(" ", "\n", "\t", ",", ".", "?", "!", ":", ";")
            .filter { it.isNotBlank() }

        val scored = kb.map { chunk ->
            val textAll = buildString {
                append(chunk.topic ?: "")
                append(' ')
                append(chunk.region ?: "")
                append(' ')
                append(chunk.text ?: "")
                append(' ')
                append(chunk.knot_name ?: "")
                append(' ')
                append(chunk.description ?: "")
            }.lowercase()

            val score = queryWords.count { word -> textAll.contains(word) }
            chunk to score
        }

        return scored
            .sortedByDescending { it.second }
            .take(k)
            .map { it.first }
    }

    /** Turn a KnowledgeChunk into a human-readable context passage. */
    private fun KnowledgeChunk.asContextText(): String {
        val baseText = when {
            text != null -> text
            description != null -> {
                val title = topic ?: knot_name ?: "Knot"
                val uses = use_cases?.joinToString(", ") ?: ""
                val instr = instructions ?: ""
                "$title.\nDescription: $description\nUses: $uses\nInstructions: $instr"
            }
            else -> ""
        }
        return baseText
    }

    private fun cosine(a: FloatArray, b: FloatArray): Double {
        val dot = a.indices.sumOf { (a[it] * b.getOrElse(it) { 0f }).toDouble() }
        val magA = kotlin.math.sqrt(a.sumOf { (it * it).toDouble() })
        val magB = kotlin.math.sqrt(b.sumOf { (it * it).toDouble() })
        return if (magA == 0.0 || magB == 0.0) 0.0 else dot / (magA * magB)
    }

    private fun copyAssetToCache(name: String): File {
        val out = File(app.cacheDir, name)
        if (!out.exists()) {
            app.assets.open(name).use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
        }
        return out
    }
}
