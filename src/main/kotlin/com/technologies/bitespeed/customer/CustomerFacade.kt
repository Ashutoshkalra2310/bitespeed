package com.technologies.bitespeed.customer

import com.technologies.bitespeed.customer.repository.Customer
import com.technologies.bitespeed.customer.repository.LinkedPrecedence
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.jvm.Throws

@Component
@Slf4j
class CustomerFacade(
    private val customerService: CustomerService
) {
    private val log = LoggerFactory.getLogger(CustomerService::class.java)

    @Throws(Exception::class)
    fun getCustomerDetails(customerDto: CustomerRequestDto): CustomerResponseDto? {
        log.info("inside getCustomerDetails")

        if(customerDto.email== null && customerDto.phoneNumber == null) throw Exception("email and phoneNumber both cannot be null at same time!")
        if(customerDto.email== null || customerDto.phoneNumber == null) return customerService.getDetails(customerDto.email, customerDto.phoneNumber)
        val customerWithSameEmail = customerDto.email?.let { customerService.getCustomerWithSameEmail(it) }
        var primaryCustomerWithSameEmail = customerWithSameEmail?.filter { it.linkedPrecedence == LinkedPrecedence.PRIMARY }

        if(primaryCustomerWithSameEmail?.isEmpty() == false && primaryCustomerWithSameEmail.count() >1) throw Exception("corrupt data, email: ${customerDto.email} has two primary precedence")

        if(customerWithSameEmail.isNullOrEmpty() == false && primaryCustomerWithSameEmail.isNullOrEmpty() == true) primaryCustomerWithSameEmail = customerService.getPrimaryCustomerFromSecondaryCustomer(customerWithSameEmail.first())

        val customerWithSamePhone = customerDto.phoneNumber?.let { customerService.getCustomerWithSamePhone(it) }
        var primaryCustomerWithSamePhone = customerWithSamePhone?.filter { it.linkedPrecedence == LinkedPrecedence.PRIMARY }

        if(primaryCustomerWithSamePhone?.isEmpty() == false && primaryCustomerWithSamePhone.count() >1) throw Exception("corrupt data, number: ${customerDto.phoneNumber} has two primary precedence")

        if(customerWithSamePhone.isNullOrEmpty() == false && primaryCustomerWithSamePhone.isNullOrEmpty() == true) primaryCustomerWithSamePhone = customerService.getPrimaryCustomerFromSecondaryCustomer(customerWithSamePhone.first())

        return this.updateCustomerData(customerDto, primaryCustomerWithSameEmail?.firstOrNull(), primaryCustomerWithSamePhone?.firstOrNull())
    }

    @Throws(Exception::class)
    fun updateCustomerData(customerRequestDto: CustomerRequestDto, primaryCustomerWithSameEmail: Customer?, primaryCustomerWithSamePhone: Customer?): CustomerResponseDto? {
        log.info("inside updateCustomerData")
        val email = customerRequestDto.email
        val number = customerRequestDto.phoneNumber
        if(primaryCustomerWithSameEmail == null && primaryCustomerWithSamePhone == null){
            return customerService.createNewCustomer(customerRequestDto.email, customerRequestDto.phoneNumber)
        }
        else if(primaryCustomerWithSameEmail == null && (email != null || number != null)){
            return primaryCustomerWithSamePhone?.let { customerService.createNewSecondaryCustomer(it, email, number) }
        }
        else if(primaryCustomerWithSamePhone == null && (email != null || number != null)){
            return primaryCustomerWithSameEmail?.let { customerService.createNewSecondaryCustomer(it, email, number) }
        }
        else if(primaryCustomerWithSameEmail != null && primaryCustomerWithSamePhone != null){
            return customerService.updateCustomerData(primaryCustomerWithSameEmail, primaryCustomerWithSamePhone)
        }
        else return null
    }
}