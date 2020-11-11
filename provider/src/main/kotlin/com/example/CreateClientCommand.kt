package com.example

import kotlinx.serialization.Serializable

@Serializable
data class CreateClientCommand(val firstName: String, val lastName: String, val age: Int) {

    fun toClient(id: Long) = Client(
        id = id,
        firstName = firstName,
        lastName = lastName,
        age = age
    )

    fun validate(): Map<String, String> {
        val errors: MutableMap<String, String> = mutableMapOf()

        if (age < 0) {
            errors["client.age.invalid"] = "The age should be a positive number"
        }

        if (firstName.isBlank() || firstName.isEmpty()) {
            errors["client.firstName.invalid"] = "The first name cannot be blank of empty"
        }

        if (lastName.isBlank() || lastName.isEmpty()) {
            errors["client.lastName.invalid"] = "The last name cannot be blank of empty"
        }

        return errors.toMap()
    }
}