package com.example.ai_project.mediapipe_practice.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ai_project.R
import com.example.ai_project.databinding.ActivityChatBinding
import com.example.ai_project.databinding.ActivityMainBinding
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var llmInference: LlmInference
//    private val modelFileName = "gemma-3n-E4B-it-int4.task" // Update this if using different model
private val modelFileName = "gemma3-1b-it-int4.task"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeModel()

        binding.generateButton.setOnClickListener {
            val prompt = binding.promptInput.text.toString()
            if (prompt.isBlank()) {
                Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateResponse(prompt)
        }
    }

    private fun initializeModel() {
        binding.progressBar.visibility = View.VISIBLE

        Log.d("ModelInit", "Starting model initialization")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Prepare model file with verification
                val modelFile = prepareModelFile().also {
                    Log.d("ModelInit", "Model file prepared: ${it.absolutePath}")
                }

                // Step 2: Build options with cache configuration
                val options = buildModelOptions()

                // Step 3: Initialize LLM
                val inference = LlmInference.createFromOptions(this@ChatActivity, options)

                // Step 4: Update UI on success
                withContext(Dispatchers.Main) {
                    llmInference = inference
                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(this@ChatActivity, "Model initialized", Toast.LENGTH_SHORT).show()
                    Log.d("ModelInit", "Model initialized successfully")
                }

            } catch (e: Exception) {
                Log.e("ModelInit", "Initialization failed", e)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    showErrorDialog("Model initialization failed: ${e.message}")
                }
            }
        }
    }

    private fun prepareModelFile(): File {
        val internalModelFile = File(filesDir, modelFileName)

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

    private fun buildModelOptions(): LlmInference.LlmInferenceOptions {
        return LlmInference.LlmInferenceOptions.builder()
            .setModelPath(File(filesDir, modelFileName).absolutePath)
            .setMaxTokens(512)
            .setMaxTopK(40)
            .apply {
                // Configure cache directory
                val cacheDir = File(cacheDir, "tflite_cache").apply { mkdirs() }
                System.setProperty("org.tensorflow.lite.xnnpack.cache.dir", cacheDir.absolutePath)

                // Alternative: Disable XNNPACK if still having issues
                // setUseXNNPACK(false)
            }
            .build()
    }

    private fun generateResponse(prompt: String) {
        if (!::llmInference.isInitialized) {
            Toast.makeText(this, "Model not ready", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        binding.resultText.text = ""

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    llmInference.generateResponse(prompt)
                }
                binding.resultText.text = result

            } catch (e: Exception) {
                Log.e("Generation", "Error generating response", e)
                binding.resultText.text = "Error: ${e.message}"

            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showErrorDialog(message: String, fatal: Boolean = false) {
        AlertDialog.Builder(this)
            .setTitle(if (fatal) "Fatal Error" else "Error")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ -> if (!fatal) initializeModel() }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .setCancelable(!fatal)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::llmInference.isInitialized) {
            llmInference.close()
        }
    }
}