package com.technologies.bitespeed

class ResponseWrapper <T> (
    val data: T?,
    val message: String?,
    val success: Boolean
)