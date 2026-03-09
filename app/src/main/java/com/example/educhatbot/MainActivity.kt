package com.example.educhatbot

// LƯU Ý: Giữ nguyên dòng 'package com.example.educhatbot' của máy bạn ở trên cùng này nhé!

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button

    private val messageList = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    // Biến này lưu lại toàn bộ lịch sử chat để gửi lên API, giúp AI "nhớ" ngữ cảnh
    private val apiMessageHistory = mutableListOf<OpenAIMessage>()

    // 🔑 DÁN API KEY CỦA BẠN VÀO ĐÂY (Nằm trong cặp dấu ngoặc kép)
    private val apiKey = "DÁN_API_KEY_CỦA_BẠN_VÀO_ĐÂY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ánh xạ các view từ giao diện XML
        recyclerView = findViewById(R.id.recyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)

        // Cài đặt RecyclerView (Danh sách chat)
        adapter = MessageAdapter(messageList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 1. Cài đặt "Tính cách" cho AI (System Prompt)
        setupSystemPrompt()

        // 2. Xử lý khi bấm nút Gửi
        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }
    }

    // Hàm thiết lập vai trò Gia sư cho AI
    private fun setupSystemPrompt() {
        val prompt = "Bạn là một gia sư nhiệt tình và thông minh. Nhiệm vụ của bạn là hướng dẫn học sinh giải bài tập bằng cách đưa ra từng gợi ý nhỏ, giải thích phương pháp. TUYỆT ĐỐI KHÔNG giải hộ hay đưa ra đáp án cuối cùng ngay lập tức. Hãy khơi gợi để học sinh tự tìm ra kết quả."
        // Đưa câu lệnh này vào lịch sử với vai trò là "system" (Hệ thống)
        apiMessageHistory.add(OpenAIMessage(role = "system", content = prompt))
    }

    private fun sendMessage(userText: String) {
        // --- PHẦN 1: XỬ LÝ GIAO DIỆN ---
        // Hiện tin nhắn của mình (User) lên màn hình
        messageList.add(Message(userText, true))
        adapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
        etMessage.setText("") // Xóa trắng ô nhập liệu

        // Lưu tin nhắn của User vào lịch sử API
        apiMessageHistory.add(OpenAIMessage(role = "user", content = userText))

        // Tạo hiệu ứng "AI đang suy nghĩ..."
        messageList.add(Message("AI đang suy nghĩ...", false))
        val typingPosition = messageList.size - 1
        adapter.notifyItemInserted(typingPosition)
        recyclerView.scrollToPosition(typingPosition)

        // Khóa nút Gửi tạm thời để tránh bấm 2 lần liên tục
        btnSend.isEnabled = false

        // --- PHẦN 2: GỌI API CHẠY NGẦM ---
        // lifecycleScope.launch giúp việc gọi mạng chạy ngầm, không làm đơ ứng dụng
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Gom toàn bộ lịch sử chat gửi lên server
                val request = ChatRequest(messages = apiMessageHistory)

                // Gửi Request đi và chờ Response về
                val response = ApiClient.instance.getChatCompletion("Bearer $apiKey", request)

                // Khi có kết quả, quay lại luồng chính (Main) để cập nhật giao diện
                withContext(Dispatchers.Main) {
                    // Xóa dòng chữ "AI đang suy nghĩ..."
                    messageList.removeAt(typingPosition)
                    adapter.notifyItemRemoved(typingPosition)

                    if (response.isSuccessful && response.body() != null) {
                        // Trích xuất câu trả lời của AI từ gói JSON
                        val aiReply = response.body()!!.choices[0].message.content

                        // Hiện tin nhắn của AI lên màn hình
                        messageList.add(Message(aiReply, false))
                        adapter.notifyItemInserted(messageList.size - 1)
                        recyclerView.scrollToPosition(messageList.size - 1)

                        // LƯU Ý QUAN TRỌNG: Lưu câu trả lời của AI vào lịch sử để nó nhớ
                        apiMessageHistory.add(OpenAIMessage(role = "assistant", content = aiReply))
                    } else {
                        // Nếu server báo lỗi (Hết hạn API, Sai đường dẫn...)
                        Toast.makeText(this@MainActivity, "Lỗi Server: ${response.code()}", Toast.LENGTH_LONG).show()
                    }

                    btnSend.isEnabled = true // Mở khóa nút gửi
                }

            } catch (e: Exception) {
                // Xử lý lỗi mất mạng hoặc lỗi không xác định
                withContext(Dispatchers.Main) {
                    messageList.removeAt(typingPosition)
                    adapter.notifyItemRemoved(typingPosition)
                    Toast.makeText(this@MainActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSend.isEnabled = true
                }
            }
        }
    }
}