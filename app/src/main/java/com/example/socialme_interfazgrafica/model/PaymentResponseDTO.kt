package com.example.socialme_interfazgrafica.model

data class PaymentResponseDTO(
    val success: Boolean,
    val paymentId: String? = null,
    val approvalUrl: String? = null,
    val message: String,
    val amount: Double? = null,
    val currency: String? = null
)
