package com.example.ai_project.chat_app.activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ai_project.R
import com.example.ai_project.chat_app.adapter.ChatAdapter
import com.example.ai_project.chat_app.adapter.ChatMessage
import com.example.ai_project.chat_app.view_model.ChatViewModel
import com.example.ai_project.databinding.ActivityChat2Binding
import com.example.ai_project.databinding.ActivityChatBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChat2Binding
    private val viewModel by viewModels<ChatViewModel>()
    private val chatAdapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChat2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge()
        setupUI()
        observeViewModel()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        binding.chatRecyclerView.itemAnimator = DefaultItemAnimator().apply {
            addDuration = 200
            changeDuration = 200
            moveDuration = 200
            removeDuration = 200
        }

        binding.chatRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // This makes messages start from bottom
                reverseLayout = false // Keep this false for normal order
            }
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
        }

        setupKeyboardListener()
        setupSendButton()
    }

    private fun setupKeyboardListener() {
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            binding.root.getWindowVisibleDisplayFrame(rect)
            val screenHeight = binding.root.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            if (keypadHeight > screenHeight * 0.15) {
                binding.chatRecyclerView.post {
                    scrollToBottom()
                }
            }
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val prompt = binding.promptInput.text.toString()
            if (prompt.isNotBlank()) {
                hideKeyboard()
                addUserMessage(prompt)
                viewModel.sendMessage(prompt)
                binding.promptInput.text?.clear()
            }
        }

        binding.promptInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                binding.sendButton.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.promptInput.windowToken, 0)
    }

//    private fun observeViewModel() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.uiState.collect { state ->
//                    when (state) {
//                        is ChatViewModel.ChatUiState.Loading -> showLoading()
//                        is ChatViewModel.ChatUiState.Ready -> showReady()
//                        is ChatViewModel.ChatUiState.Generating -> showGenerating()
//                        is ChatViewModel.ChatUiState.MessageReceived -> showMessage(state.response)
//                        is ChatViewModel.ChatUiState.Error -> showError(state.message)
//                    }
//                }
//            }
//        }
//    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Log.d("ChatActivity", "New state received: $state")
                    when (state) {
                        is ChatViewModel.ChatUiState.Loading -> showLoading()
                        is ChatViewModel.ChatUiState.Ready -> showReady()
                        is ChatViewModel.ChatUiState.Generating -> showGenerating()
                        is ChatViewModel.ChatUiState.MessageReceived -> {
                            Log.d("ChatActivity", "Displaying message: ${state.response}")
                            showMessage(state.response)
                        }
                        is ChatViewModel.ChatUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showMessage(response: String) {
        runOnUiThread {
            addBotMessage(response)
            binding.sendButton.isEnabled = true
            Log.d("ChatActivity", "Bot message added to UI: $response")
        }
    }

    private fun scrollToBottom() {
        val itemCount = chatAdapter.itemCount
        if (itemCount > 0) {
            binding.chatRecyclerView.post {
                // Always scroll to bottom, no conditions
                (binding.chatRecyclerView.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(itemCount - 1, 0)
            }
        }
    }

    private fun addUserMessage(text: String) {
        val newList = chatAdapter.currentList.toMutableList().apply {
            add(ChatMessage(text = text, isUser = true))
            // Add temporary loading message
            add(ChatMessage(text = "Typing...", isUser = false, isTyping = true))
        }
        chatAdapter.submitList(newList) { scrollToBottom() }
    }

//    private fun addBotMessage(text: String) {
//        Log.d("check_ChatActivity","UI update issue -> ${text}")
//        val newList = chatAdapter.currentList.toMutableList().apply {
//            add(ChatMessage(text = text, isUser = false))
//        }
//        chatAdapter.submitList(newList) { scrollToBottom() }
//    }

    private fun addBotMessage(text: String) {
        // Remove the typing indicator and add actual response
        val newList = chatAdapter.currentList
            .filterNot { it.isTyping == true }
            .toMutableList()
            .apply {
                add(ChatMessage(text = text, isUser = false))
            }

        chatAdapter.submitList(newList) { scrollToBottom() }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.sendButton.isEnabled = false
    }

    private fun showReady() {
        binding.progressBar.visibility = View.GONE
        binding.sendButton.isEnabled = true
    }

    private fun showGenerating() {
        binding.sendButton.isEnabled = false
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.sendButton.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}