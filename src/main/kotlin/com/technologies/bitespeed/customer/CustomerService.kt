package com.technologies.bitespeed.customer

import com.technologies.bitespeed.customer.repository.Customer
import com.technologies.bitespeed.customer.repository.CustomerRepository
import com.technologies.bitespeed.customer.repository.LinkedPrecedence
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
@Slf4j
class CustomerService(
    private val customerRepository: CustomerRepository
) {

    private val log = LoggerFactory.getLogger(CustomerService::class.java)

    fun getCustomerWithSameEmail(email: String): List<Customer> {
        log.info("inside getCustomerWithSameEmail email: $email")
        return customerRepository.findByEmail(email)
    }

    fun getCustomerWithSamePhone(number: String): List<Customer> {
        log.info("inside getCustomerWithSamePhone number: $number")
        return customerRepository.findByPhoneNumber(number)
    }

    fun getDetails(email: String?, number: String?): CustomerResponseDto? {
        log.info("inside getDetails number: $number, email: $email")
        val emailData = email?.let { getCustomerWithSameEmail(it) }
        var primaryCustomerDetailsWithEmail = emailData?.filter{it.linkedPrecedence == LinkedPrecedence.PRIMARY}?.firstOrNull()
        val secondaryCustomer = emailData?.firstOrNull()
        if(primaryCustomerDetailsWithEmail == null) primaryCustomerDetailsWithEmail = secondaryCustomer?.let {
            getPrimaryCustomerFromSecondaryCustomer(
                it
            ).firstOrNull()
        }

        val numberData = number?.let { getCustomerWithSamePhone(it) }
        var primaryCustomerDetailsWithNumber = numberData?.filter{it.linkedPrecedence == LinkedPrecedence.PRIMARY}?.firstOrNull()
        val secondaryCustomerDetails = numberData?.firstOrNull()
        if(primaryCustomerDetailsWithNumber == null) primaryCustomerDetailsWithNumber = secondaryCustomerDetails?.let {
            getPrimaryCustomerFromSecondaryCustomer(
                it
            ).firstOrNull()
        }

        if(primaryCustomerDetailsWithEmail == null && primaryCustomerDetailsWithNumber == null) {
            throw Exception("No Details Found!")
        }
        else if(primaryCustomerDetailsWithNumber != null) {
            return convertCustomerToCustomerResponseDto(primaryCustomerDetailsWithNumber)
        }
        else if(primaryCustomerDetailsWithEmail != null){
            return convertCustomerToCustomerResponseDto(primaryCustomerDetailsWithEmail)
        }
        return null
    }

    fun createNewCustomer(email: String?, number: String?): CustomerResponseDto {
        log.info("inside createNewCustomer number: $number, email: $email")
        if(email == null || number == null) throw Exception("email or number cannot be null while creating a new customer")
        val customer = customerRepository.save(Customer(
            phoneNumber = number,
            email = email,
            linkedId = null,
            linkedPrecedence = LinkedPrecedence.PRIMARY,
            updatedAt = LocalDateTime.now()
        ))
        return convertCustomerToCustomerResponseDto(customer)
    }

    fun createNewSecondaryCustomer(primaryCustomer: Customer, email: String?, number: String?): CustomerResponseDto {
        log.info("inside createNewSecondaryCustomer number: $number, email: $email")
        val customer = customerRepository.save(Customer(
            phoneNumber = number ?: primaryCustomer.phoneNumber,
            email = email ?: primaryCustomer.email,
            linkedId = primaryCustomer.id,
            linkedPrecedence = LinkedPrecedence.SECONDARY,
            updatedAt = LocalDateTime.now()
        ))
        return convertCustomerToCustomerResponseDto(primaryCustomer)
    }


    fun getPrimaryCustomerFromSecondaryCustomer(secondaryCustomer: Customer): List<Customer>{
        log.info("inside getPrimaryCustomerFromSecondaryCustomer")
        val primaryCustomerId = secondaryCustomer.linkedId
        if(primaryCustomerId == null) throw Exception("Corrupt Data, no primary Id linked with ${secondaryCustomer.id}")
        return listOf(customerRepository.findById(primaryCustomerId).orElseThrow{ throw Exception("Corrupt Data, no Customer linked with ${secondaryCustomer.id}")} )
    }

    fun updateCustomerData(primaryCustomerWithSameEmail: Customer?, primaryCustomerWithSamePhone: Customer?): CustomerResponseDto? {
        log.info("inside updateCustomerData")
        if(primaryCustomerWithSameEmail?.id == primaryCustomerWithSamePhone?.id) {
            return primaryCustomerWithSameEmail?.let { convertCustomerToCustomerResponseDto(it) }
        }
        val linkedSecondaryCustomersWithSamePhone = primaryCustomerWithSamePhone?.id?.let {
            customerRepository.findByLinkedId(
                it
            )
        }
        val updatedSecondaryCustomerList: MutableList<Customer> = mutableListOf()
        linkedSecondaryCustomersWithSamePhone?.forEach {
            it.linkedId = primaryCustomerWithSameEmail?.id
            it.updatedAt = LocalDateTime.now()
            updatedSecondaryCustomerList.add(it)
        }
        customerRepository.saveAll(updatedSecondaryCustomerList)
        primaryCustomerWithSamePhone?.linkedId = primaryCustomerWithSameEmail?.id
        primaryCustomerWithSamePhone?.linkedPrecedence = LinkedPrecedence.SECONDARY
        primaryCustomerWithSamePhone?.updatedAt = LocalDateTime.now()
        primaryCustomerWithSamePhone?.let { customerRepository.save(it) }
        return primaryCustomerWithSameEmail?.let { convertCustomerToCustomerResponseDto(it) }
    }

    fun convertCustomerToCustomerResponseDto(customer: Customer): CustomerResponseDto {
        log.info("inside convertCustomerToCustomerResponseDto")
        val allDetails = customerRepository.findByLinkedId(customer.id)
        var allEmails =allDetails.map { it.email } + customer.email
        allEmails = allEmails.toSet().toList()
        var allPhoneNumbers = allDetails.map { it.phoneNumber } + customer.phoneNumber
        allPhoneNumbers = allPhoneNumbers.toSet().toList()
        var secondaryContactIds: List<Int>? = allDetails.map{it.id.toInt()}
        if(secondaryContactIds.isNullOrEmpty() == true) secondaryContactIds = null
        val customerDetailsDto = CustomerDetailsDto(
            primaryContactId =  customer.id.toInt(),
            phoneNumbers =  allPhoneNumbers,
            emails = allEmails,
            secondaryContactIds = secondaryContactIds
        )
        return CustomerResponseDto(customer = customerDetailsDto)
    }

}