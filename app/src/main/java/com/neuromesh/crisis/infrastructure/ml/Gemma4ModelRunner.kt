package com.neuromesh.crisis.infrastructure.ml

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.neuromesh.crisis.util.Logger
import com.neuromesh.crisis.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Gemma4ModelRunner @Inject constructor(
    private val context: Context,
    private val modelLoader: ModelLoader
) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val modelPath = modelLoader.getModelPath()
                ?: return@withContext Result.Error("Model file not found")

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(MAX_TOKENS)
                .setTopK(TOP_K)
                .setTemperature(TEMPERATURE)
                .setRandomSeed(RANDOM_SEED)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            Logger.d(TAG, "Gemma 4 model initialized at $modelPath")
            Result.Success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to initialize model", e)
            Result.Error("Model initialization failed: ${e.message}")
        }
    }

    suspend fun generate(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized || llmInference == null) {
            return@withContext Result.Error("Model not initialized")
        }
        try {
            val response = llmInference!!.generateResponse(prompt)
            Logger.d(TAG, "Generated response (${response.length} chars)")
            Result.Success(response)
        } catch (e: Exception) {
            Logger.e(TAG, "Generation failed", e)
            Result.Error("Generation failed: ${e.message}")
        }
    }

    suspend fun generateStreaming(
        prompt: String,
        onToken: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized || llmInference == null) {
            return@withContext Result.Error("Model not initialized")
        }
        try {
            val sb = StringBuilder()
            llmInference!!.generateResponseAsync(prompt) { partialResult, done ->
                if (partialResult != null) {
                    sb.append(partialResult)
                    onToken(partialResult)
                }
            }
            Result.Success(sb.toString())
        } catch (e: Exception) {
            Logger.e(TAG, "Streaming generation failed", e)
            Result.Error("Streaming failed: ${e.message}")
        }
    }

    fun isReady(): Boolean = isInitialized && llmInference != null

    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "Gemma4ModelRunner"
        private const val MAX_TOKENS = 1024
        private const val TOP_K = 40
        // Low temperature: we want reliable, schema-conformant JSON, not
        // creative variation. 0.7 was a major source of hallucinated crises
        // and malformed output.
        private const val TEMPERATURE = 0.2f
        private const val RANDOM_SEED = 42
    }
}