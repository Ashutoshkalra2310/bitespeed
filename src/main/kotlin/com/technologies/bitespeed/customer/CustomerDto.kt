package com.technologies.bitespeed.customer

data class CustomerRequestDto(
    val email: String?,
    val phoneNumber: String?
)

data class CustomerResponseDto(
    val customer: CustomerDetailsDto
)

data class CustomerDetailsDto(
    val primaryContactId: Int,
    val emails: List<String>,
    val phoneNumbers: List<String>,
    val secondaryContactIds: List<Int>?
)