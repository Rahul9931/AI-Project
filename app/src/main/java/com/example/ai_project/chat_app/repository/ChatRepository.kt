package com.example.ai_project.chat_app.repository

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ChatRepository(private val context: Context) {
    private lateinit var llmInference: LlmInference
    private val modelFileName = "gemma3-1b-it-int4.task"

    private suspend fun prepareModelFile(): File {
        val internalModelFile = File(context.filesDir, modelFileName)

        // Verify existing file
        if (internalModelFile.exists()) {
            if (internalModelFile.length() > 0) {
                return internalModelFile
            } else {
                internalModelFile.delete()
                Log.w("ModelFile", "Deleted zero-byte model file")
            }
        }

        // Copy from temp location
        val tmpFile = File("/data/local/tmp/llm/$modelFileName")
        if (!tmpFile.exists()) {
            throw FileNotFoundException("Model file not found at ${tmpFile.absolutePath}")
        }

        Log.d("ModelFile", "Copying model from ${tmpFile.absolutePath}")
        tmpFile.copyTo(internalModelFile)

        // Verify copy
        if (!internalModelFile.exists() || internalModelFile.length() != tmpFile.length()) {
            throw IOException("Failed to copy model file correctly")
        }

        return internalModelFile
    }

    suspend fun initializeModel(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("ChatRepository", "Starting model initialization")
            //val modelFile = prepareModelFile()
            //Log.d("ChatRepository", "Model file path: ${modelFile.absolutePath}")

            val options = LlmInference.LlmInferenceOptions.builder()
//                .setModelPath(modelFile.absolutePath)
                .setModelPath("/data/local/tmp/llm/$modelFileName")
                .setMaxTokens(512)
                .setMaxTopK(40)
//                .apply {
//                    val cacheDir = File(context.cacheDir, "tflite_cache").apply { mkdirs() }
//                    Log.d("ChatRepository", "Cache dir: ${cacheDir.absolutePath}")
//                    System.setProperty("org.tensorflow.lite.xnnpack.cache.dir", cacheDir.absolutePath)
//                }
                .build()

            Log.d("ChatRepository", "Creating LlmInference instance")
            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("ChatRepository", "Model initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Initialization failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("ChatRepository", "Generating response for: $prompt")
            if (!::llmInference.isInitialized) {
                throw IllegalStateException("Model not initialized")
            }
            val response = llmInference.generateResponse(prompt)
            Log.d("ChatRepository", "Response received: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Generation failed", e)
            Result.failure(e)
        }
    }


    fun cleanup() {
        if (::llmInference.isInitialized) {
            llmInference.close()
        }
    }
}