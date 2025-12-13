package io.github.hziqzmin.wildguardai

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import kotlin.math.sqrt
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

        // Name of the Gemma task file INSIDE assets/
        private const val GEMMA_ASSET_NAME = "gemma3-1B-it-int4.task"

        // or 600–800, but keep it modest
        private const val EMBED_TEXT_MAX_CHARS = 600

        // ~100–150 tokens
        private const val CONTEXT_CHARS_PER_CHUNK = 400

        // How many passages to retrieve for context
        private const val TOP_K = 1

        // Safety cap for prompt length to avoid maxTokens crash
        private const val MAX_PROMPT_CHARS = 7000
    }

    // MediaPipe TextEmbedder for embeddings
    private var textEmbedder: TextEmbedder? = null

    // Embeddings for each KB item, same index as `library`
    private var kbEmbeddings: List<FloatArray> = emptyList()

    // Embedding model (TFLite) for similarity search
    //private var finder: Interpreter? = null

    // LLM for generation (Gemma via MediaPipe)
    private var brain: LlmInference? = null

    // Per-session sampling controls (temperature, top-k, etc.)
    private var brainSession: LlmInferenceSession? = null

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
                    append("Hi! I'm WildGuard AI (Gemma on-device). How can I help you?\n")
                } else {
                    append("AI model is not loaded or failed to respond.\n")
                    append("\nExpected .task file in assets: $GEMMA_ASSET_NAME")
                }

                if (textEmbedder == null || kbEmbeddings.isEmpty()) {
                    append("\n⚠️ Embedding model not loaded, using keyword-only retrieval.")
                }

                /*if (finder == null) {
                    append("\n⚠️ Embedding model not loaded, using keyword-only retrieval.")
                }*/

                if (library.isEmpty()) {
                    append("\n⚠️ Knowledge base is empty or failed to load.")
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

    // ----------------- INIT -----------------

    private fun initializeAI() {
        Log.d(TAG, "Initializing AI...")

        // 1) Load knowledge base
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

        // 2) Init MediaPipe TextEmbedder and build KB embeddings
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("embedding_model.tflite") // your text embedding model
                .build()

            val options = TextEmbedderOptions.builder()
                .setBaseOptions(baseOptions)
                .setL2Normalize(true)   // so cosine = dot product
                .setQuantize(false)
                .build()

            textEmbedder = TextEmbedder.createFromOptions(app, options)
            Log.d(TAG, "TextEmbedder initialized")

            if (library.isNotEmpty()) {
                kbEmbeddings = library.map { chunk ->
                    val fullTextForChunk = chunk.asContextText().ifBlank { chunk.text.orEmpty() }
                    val textForChunk = fullTextForChunk.take(EMBED_TEXT_MAX_CHARS)  // 🔹 limit embed size
                    if (textForChunk.isBlank()) {
                        FloatArray(0)
                    } else {
                        embedText(textForChunk)
                    }
                }
                Log.d(TAG, "KB embeddings built for ${kbEmbeddings.size} chunks")
            }

        } catch (e: Exception) {
            textEmbedder = null
            kbEmbeddings = emptyList()
            Log.e(TAG, "Failed to init TextEmbedder. Will fallback to keyword search.", e)
        }

        // 3) Load Gemma LLM from assets → internal storage
        try {
            val modelPath = copyAssetToFile(GEMMA_ASSET_NAME, GEMMA_ASSET_NAME)
            Log.d(TAG, "Loading Gemma model from: $modelPath")

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(800) // increase from 256 to reduce truncation
                .setMaxTopK(40)
                .setPreferredBackend(LlmInference.Backend.CPU)
                .build()

            val engine = LlmInference.createFromOptions(app, options)
            brain = engine

            // 🔹 create first session with temperature / top-k
            val session = createSession()
            Log.d(TAG, "Gemma + session loaded ok: ${session != null}")
        } catch (e: Exception) {
            brain = null
            brainSession = null
            Log.e(TAG, "Failed to load Gemma LLM", e)
        }

    }


    // ----------------- CHAT -----------------

    fun sendMessage(userText: String) {
        if (_isAITyping.value) return

        // 1) Add user message to the UI
        _messages.value += ChatMessage(messages.value.size, userText, Author.USER)

        // 2) Make sure the model itself is loaded
        val llm = brain
        if (llm == null) {
            _messages.value += ChatMessage(
                id = messages.value.size,
                text = "AI model is not loaded. Make sure $GEMMA_ASSET_NAME is in the assets folder.",
                author = Author.AI
            )
            return
        }

        _isAITyping.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1) Retrieve relevant knowledge
                val topChunks: List<KnowledgeChunk> =
                    if (textEmbedder != null && kbEmbeddings.isNotEmpty()) {
                        try {
                            findTopMatchesWithEmbedder(userText, TOP_K)
                        } catch (e: Exception) {
                            Log.e(TAG, "Embedding failed, falling back to keyword search", e)
                            findTopMatchesByKeyword(userText, library, TOP_K)
                        }
                    } else {
                        findTopMatchesByKeyword(userText, library, TOP_K)
                    }

                // DEBUG: see what RAG picked
                Log.d(TAG, "Top chunks for query: \"$userText\"")
                topChunks.forEachIndexed { i, chunk ->
                    Log.d(TAG, "  [$i] ${chunk.topic ?: chunk.knot_name ?: "no title"}")
                }

                val context = if (topChunks.isEmpty()) {
                    "No specific survival notes were found for this question in the knowledge base."
                } else {
                    topChunks.joinToString("\n\n") { chunk ->
                        val snippet = chunk.asContextText().take(CONTEXT_CHARS_PER_CHUNK)
                        "- $snippet"
                    }
                }

                // 2) Build compact RAG prompt
                var prompt = """
                You are WildGuard AI, an offline wilderness survival assistant running entirely offline on the user’s phone. 
                
                You will receive: 
                - A curated wilderness survival knowledge base with embedding and information("knowledge_base") 
                - A user question ("Question") 
                
                Rules: 
                1. Use ONLY information from the Knowledge base to generate the best answer for the question. If the information from the knowledge base seems out of context, ignore it.
                2. If the Knowledge base does not clearly contain the information needed for a safe and specific answer: 
                    "I’m not sure based on the current knowledge base."
                    Do not add anything else before or after this sentence.
                3. Do not invent or guess new survival techniques, numbers, medical advice, or facts that are not supported by the Knowledge base. 
                4. If parts of the Knowledge base are unrelated to the Question, ignore them completely. 
                5. Prefer information that directly matches the Question’s topic (e.g. “fire”, “shelter”, “hypothermia”) over more generic or less relevant notes. 
                6. If the question is related to knots, mention the name of the knot first. 
                7. Prevent from repeating the same answers many time.
                
                Knowledge base: $context 
                
                Question: $userText 
                
                Answer format: 
                - Be precise and direct.
                - Start with ONE short sentence summarising the key idea (max 20 words).
                - Then give 3–6 numbered steps. 
                - Do NOT repeat the same idea in multiple steps.
                - After writing the information from the Knowledge base, include a final line for important cautions if needed: 
                    "Caution:" + add a suitable caution text for the user depending on the context of the answer. 
                - If the Question is outside wilderness survival, about general chit-chat, or cannot be answered safely from the Knowledge base, reply exactly: "I’m not sure based on the current knowledge base."
                
                Now write the answer following all the rules above.
                Answer: 
                """.trimIndent()

                if (prompt.length > MAX_PROMPT_CHARS) {
                    prompt = prompt.take(MAX_PROMPT_CHARS) +
                            "\n\n[Context truncated for size; answer based on the visible part only.]"
                }

                // 3) Generate with a fresh session for this question
                Log.d(TAG, "Calling session.generateResponse()")

                val session = createSession()
                if (session == null) {
                    Log.e(TAG, "Session is null even after createSession()")
                    _messages.value += ChatMessage(
                        id = messages.value.size,
                        text = "AI model session is not ready. Please restart the app.",
                        author = Author.AI
                    )
                    return@launch
                }

                session.addQueryChunk(prompt)
                val result = session.generateResponse()
                val cleaned = result.trim()

                val finalText =
                    if (cleaned.isBlank() || cleaned == "..." || cleaned.length < 10) {
                        "I couldn't generate a clear answer from the current knowledge base. " +
                                "Try asking in a simpler way, like: \"What are the steps to treat hypothermia?\""
                    } else {
                        cleaned
                    }
                Log.d(TAG, "LLM returned: ${finalText.take(120)}...")
                Log.d(TAG, "LLM result length: ${result.length}")


                _messages.value += ChatMessage(
                    id = messages.value.size,
                    text = finalText,
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


    // ----------------- RAG HELPERS -----------------

    /** Run the MediaPipe TextEmbedder on a single piece of text. */
    private fun embedText(text: String): FloatArray {
        val embedder = textEmbedder ?: error("TextEmbedder not initialized")

        val result = embedder.embed(text)
        val embeddingResult = result.embeddingResult()
        val embeddings = embeddingResult.embeddings()

        if (embeddings.isEmpty()) return FloatArray(0)

        // Single-head model → just take the first embedding
        return embeddings[0].floatEmbedding()
    }

    /** Cosine-similarity retrieval using MediaPipe TextEmbedder embeddings
     *  + strong preference for topic/title matches.
     */
    private fun findTopMatchesWithEmbedder(
        query: String,
        k: Int
    ): List<KnowledgeChunk> {
        if (library.isEmpty() || kbEmbeddings.isEmpty()) return emptyList()

        val qVec = embedText(query)
        if (qVec.isEmpty()) return emptyList()

        // 1) Extract meaningful words from the query (ignore stopwords)
        val stopwords = setOf(
            "how", "what", "when", "where", "why", "who",
            "to", "a", "an", "the", "is", "are", "do", "i",
            "you", "in", "of", "for", "on", "and", "or",
            "start", "make" // ← you can REMOVE these if you want them to count
        )

        val queryWords = query
            .lowercase()
            .split(" ", "\n", "\t", ",", ".", "?", "!", ":", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val contentWords = queryWords.filter { it !in stopwords }.toSet()
        if (contentWords.isEmpty()) {
            // Fallback: just use all words if everything got filtered out
            contentWords.toMutableSet().addAll(queryWords)
        }

        data class Scored(
            val chunk: KnowledgeChunk,
            val sim: Double,
            val titleScore: Int,
            val bodyScore: Int
        )

        val scored = library.mapIndexedNotNull { index, chunk ->
            val vec = kbEmbeddings.getOrNull(index)
            if (vec == null || vec.isEmpty() || vec.size != qVec.size) {
                null
            } else {
                val sim = cosine(qVec, vec)

                val titleText = buildString {
                    append(chunk.topic ?: "")
                    append(' ')
                    append(chunk.knot_name ?: "")
                }.lowercase()

                val bodyText = buildString {
                    append(chunk.region ?: "")
                    append(' ')
                    append(chunk.text ?: "")
                    append(' ')
                    append(chunk.description ?: "")
                }.lowercase()

                val titleScore = contentWords.count { word -> titleText.contains(word) }
                val bodyScore = contentWords.count { word -> bodyText.contains(word) }

                Scored(chunk, sim, titleScore, bodyScore)
            }
        }

        if (scored.isEmpty()) return emptyList()

        // 2) Prefer items where the TITLE matches the important words
        val withTitleMatches = scored.filter { it.titleScore > 0 }

        val baseList = when {
            withTitleMatches.isNotEmpty() -> withTitleMatches
            else -> scored // no title matches at all → fall back to everything
        }

        val sorted = baseList.sortedWith(
            compareByDescending<Scored> { it.titleScore }  // 1) topic/knot_name overlap
                .thenByDescending { it.bodyScore }         // 2) body overlap
                .thenByDescending { it.sim }               // 3) embedding similarity
        )

        return sorted.take(k).map { it.chunk }
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
        val len = minOf(a.size, b.size)
        var dot = 0.0
        var magA = 0.0
        var magB = 0.0
        for (i in 0 until len) {
            val va = a[i].toDouble()
            val vb = b[i].toDouble()
            dot += va * vb
            magA += va * va
            magB += vb * vb
        }
        if (magA == 0.0 || magB == 0.0) return 0.0
        return dot / (sqrt(magA) * sqrt(magB))
    }

    /** Copy big files (like .task) to internal storage and return full path. */
    private fun copyAssetToFile(assetName: String, outFileName: String): String {
        val file = File(app.filesDir, outFileName)
        if (!file.exists()) {
            app.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    // ----------------- LLM SESSION HELPERS -----------------

    /**
     * Create a fresh LlmInferenceSession with our sampling settings.
     * Also closes any old session. Returns the new session or null on failure.
     */
    private fun createSession(): LlmInferenceSession? {
        val engine = brain ?: return null

        try {
            brainSession?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing old session", e)
        }

        val sessionOptions =
            LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(20)          // how many tokens to consider
                .setTopP(0.8f)       // nucleus sampling
                .setTemperature(0.2f) // lower = more deterministic, fewer hallucinations
                .build()

        val session = LlmInferenceSession.createFromOptions(engine, sessionOptions)
        brainSession = session
        return session
    }

    /**
     * Optional: call this if you add a "New chat" button in the UI.
     * It recreates the session and resets the chat messages.
     */
    fun resetChatSession() {
        createSession()
        _messages.value = listOf(
            ChatMessage(
                id = 0,
                text = "Hi! I'm WildGuard AI (Gemma on-device). How can I help you?",
                author = Author.AI
            )
        )
    }

}
