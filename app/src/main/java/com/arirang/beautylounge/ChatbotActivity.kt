package com.arirang.beautylounge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityChatbotBinding

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    private val responses = mapOf(
        "services" to "We offer: Hair Styling, Nail Care, Facial Treatments, Makeup, Waxing, Massage, and Eyebrow Threading. Which service interests you?",
        "hair" to "Our hair services include: Haircut (from R150), Hair Color (from R300), Highlights (from R400), Blowout (R120), and Hair Treatments (from R200).",
        "nail" to "Our nail services include: Manicure (R80), Pedicure (R100), Gel Nails (R150), Acrylic Nails (R200), and Nail Art (from R20 extra).",
        "facial" to "Our facial treatments include: Basic Facial (R200), Deep Cleansing Facial (R300), Anti-Aging Treatment (R350), and Brightening Facial (R280).",
        "makeup" to "We offer Bridal Makeup (R600), Party Makeup (R350), Natural Makeup (R250), and Makeup Lessons (R400 per session).",
        "waxing" to "Waxing services: Full Leg (R180), Half Leg (R120), Underarm (R80), Bikini Wax (R150), and Full Body Wax (R500).",
        "massage" to "Massage services: Swedish Massage (R300/hr), Deep Tissue Massage (R350/hr), Hot Stone Massage (R400/hr), and Back Massage (R200/30min).",
        "eyebrow" to "Eyebrow threading costs R50. Eyebrow tinting is R80. Combined threading + tinting is R120.",
        "book" to "To book an appointment, tap 'My Bookings' on your dashboard. You can choose your service, preferred date, and a stylist. Bookings can be made up to 2 weeks in advance.",
        "appointment" to "Appointments are available Monday-Saturday, 8:00 AM - 6:00 PM. Sunday we operate 9:00 AM - 3:00 PM. Call us to check availability.",
        "cancel" to "To cancel an appointment, please do so at least 24 hours in advance to avoid a cancellation fee. Contact us or cancel through the app.",
        "reschedule" to "You can reschedule your appointment at least 12 hours before the appointment time. Contact us or use the app to reschedule.",
        "hours" to "We are open Monday-Saturday 8:00 AM - 6:00 PM, Sunday 9:00 AM - 3:00 PM. We are closed on public holidays.",
        "location" to "We are located in the heart of the city. Our exact address is available in the Contact section of our app.",
        "contact" to "You can reach us at: Phone: +27 12 345 6789, Email: info@arirangbeauty.co.za, or visit us at our salon.",
        "price" to "Our prices vary by service. Hair from R120, Nails from R80, Facials from R200, Makeup from R250. Ask about a specific service for exact pricing!",
        "payment" to "We accept Cash, Credit/Debit Cards, and EFT payments. No crypto payments at this time.",
        "discount" to "We offer a 10% loyalty discount for repeat customers. Students get 15% off on Tuesdays with valid ID. Ask about our special packages!",
        "stylist" to "All our stylists are professionally trained and certified. When booking, you can request a specific stylist based on availability.",
        "staff" to "Our team includes experienced hair stylists, nail technicians, estheticians, and makeup artists. Each specialist is trained in the latest techniques.",
        "hello" to "Hello! Welcome to Arirang Beauty Lounge! \uD83D\uDC85 How can I help you today? You can ask about our services, pricing, booking, or anything else!",
        "hi" to "Hi there! \uD83D\uDC4B Welcome to Arirang Beauty Lounge! How can I assist you? Ask me about services, prices, or bookings!",
        "hey" to "Hey! Welcome to Arirang Beauty Lounge! \uD83C\uDF38 What can I help you with today?",
        "help" to "I can help you with:\n• Service information & pricing\n• Booking appointments\n• Our operating hours\n• Contact details\n• Special offers\n\nJust type your question!",
        "thank" to "You're welcome! \uD83D\uDE0A Is there anything else I can help you with?",
        "bye" to "Goodbye! Thank you for visiting Arirang Beauty Lounge! \uD83C\uDF38 We look forward to seeing you soon!",
        "good" to "Thank you! We're glad you're enjoying our services! \uD83D\uDE0A",
        "package" to "We offer special packages: Bridal Package (R1500 - hair + makeup + nails), Pamper Package (R800 - facial + massage + nails), and the VIP Package (R2000 - full day of beauty). Contact us for details!",
        "special" to "Current specials: Tuesdays - 15% off for students, Monthly Package deals, Loyalty program for repeat customers. Follow us on social media for more deals!",
        "loyalty" to "Our loyalty program gives you 1 point per R10 spent. Collect 100 points for a R50 voucher. Ask at reception to join!",
        "hygiene" to "We maintain the highest hygiene standards. All tools are sanitized between clients, we use fresh towels for each customer, and our premises are cleaned daily.",
        "parking" to "We have free parking available adjacent to our salon. There is also street parking nearby.",
        "wifi" to "Yes, we offer free WiFi for clients. Ask at reception for the password.",
        "products" to "We use and sell premium hair and beauty products. Brands include Wella, OPI, and La Mer. Products are available for purchase at the salon."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Beauty Assistant"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        sendBotMessage(
            "Hello! \uD83D\uDC4B I'm your Arirang Beauty Lounge assistant! How can I help you today?\n\n" +
            "You can ask me about:\n• Services & pricing\n• Booking appointments\n" +
            "• Operating hours\n• Special offers\n\nType 'help' to see all topics!"
        )

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendUserMessage(message)
                binding.etMessage.text.clear()
                processResponse(message)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatbotActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatbotActivity.adapter
        }
    }

    private fun sendUserMessage(message: String) {
        messages.add(ChatMessage(message, true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun sendBotMessage(message: String) {
        messages.add(ChatMessage(message, false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvChat.scrollToPosition(messages.size - 1)
    }

    private fun processResponse(userMessage: String) {
        val lowerMessage = userMessage.lowercase()

        var response = "I'm sorry, I don't quite understand that. Could you rephrase your question? Type 'help' to see what I can assist with! \uD83D\uDE0A"

        for ((keyword, reply) in responses) {
            if (lowerMessage.contains(keyword)) {
                response = reply
                break
            }
        }

        binding.btnSend.isEnabled = false
        binding.btnSend.visibility = View.INVISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            sendBotMessage(response)
            binding.btnSend.isEnabled = true
            binding.btnSend.visibility = View.VISIBLE
        }, 800)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
