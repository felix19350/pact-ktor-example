package com.example

import kotlinx.serialization.Serializable

@Serializable
data class Client(val id:Long, val firstName:String, val lastName:String, val age: Int)