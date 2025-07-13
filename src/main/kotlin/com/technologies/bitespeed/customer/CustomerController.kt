package com.technologies.bitespeed.customer

import com.technologies.bitespeed.ResponseWrapper
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("identify")
@Slf4j
class CustomerController(
    private val customerFacade: CustomerFacade
) {

    private val log = LoggerFactory.getLogger(CustomerService::class.java)

    @PostMapping("")
    fun getCustomerDetails(@RequestBody customerDto: CustomerRequestDto): ResponseWrapper<CustomerResponseDto?> {
        log.info("inside getCustomerDetails")
        try {
            return ResponseWrapper(success = true, data = customerFacade.getCustomerDetails(customerDto), message = null)
        } catch (ex: Exception) {
            log.error("ex: ${ex.stackTrace}")
            return ResponseWrapper(success = false, data = null, message = ex.message)
        }
    }

}