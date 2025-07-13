package com.technologies.bitespeed.customer.repository

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.springframework.data.jpa.repository.JpaRepository

@Entity(name = "customer")
class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String = "",

    @Column(name = "email", nullable = false)
    var email: String = "",

    @Column(name = "linked_id")
    var linkedId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "linked_precedence", columnDefinition = "linked_precedence_enum")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    var linkedPrecedence: LinkedPrecedence? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
) : Serializable


enum class LinkedPrecedence(val value: String) {
    PRIMARY("primary"),
    SECONDARY("secondary");

    companion object {
        fun valToEnum(value: String?): LinkedPrecedence? {
            return value?.let { nonNullValue ->
                values().find { it.value == nonNullValue }
            }
        }
    }
}


interface CustomerRepository: JpaRepository<Customer, Long> {
    fun findByEmail(email: String): List<Customer>
    fun findByPhoneNumber(phoneNumber: String): List<Customer>
    fun findByLinkedId(linkedId: Long): List<Customer>
}
