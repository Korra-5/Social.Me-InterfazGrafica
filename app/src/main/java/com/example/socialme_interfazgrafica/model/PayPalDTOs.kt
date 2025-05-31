package com.example.socialme_interfazgrafica.model

// DTOs para PayPal
data class PaymentRequestDTO(
    val username: String,
    val amount: Double,
    val description: String,
    val currency: String = "EUR"
)

data class PaymentResponseDTO(
    val success: Boolean,
    val paymentId: String? = null,
    val approvalUrl: String? = null,
    val message: String,
    val amount: Double? = null,
    val currency: String? = null
)

data class PaymentVerificationDTO(
    val paymentId: String,
    val payerId: String,
    val username: String
)

data class PaymentStatusDTO(
    val paymentId: String,
    val status: String,
    val amount: Double,
    val currency: String,
    val description: String,
    val createTime: String,
    val updateTime: String? = null
)