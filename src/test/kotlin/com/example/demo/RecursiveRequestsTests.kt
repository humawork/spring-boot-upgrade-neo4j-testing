package com.example.demo

import com.example.demo.base.BaseTests
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus.OK

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecursiveRequestsTests : BaseTests() {

    @Test
    fun createOrganization() {
        "/organization".POST(
            json {
                "name" to "MyOrg"
            }
        ).asClue { response ->
            response.statusCode shouldBe OK
        }
    }

    @Test
    fun createOrganizationWithUsers() {
        val organizationId = "/organization".POST(json { "name" to "MyOrg" }).asClue { response ->
            response.statusCode shouldBe OK
            response.body!!.path("id").toUUID()
        } // 1 cypher request

        val olaId = createUser(organizationId, "Ola", "Nordmann") // 6 cypher requests
        val kariId = createUser(organizationId, "Kari", "Nordmann", supervisor = olaId) // 15 cypher requests
        val hansId = createUser(organizationId, "Hans", "Nordmann", supervisor = kariId) // 22 cypher requests
        val siriId = createUser(organizationId, "Siri", "Nordmann", supervisor = hansId) // 29 cypher requests

        findUser(organizationId, siriId) // 10 cypher requests

        println("Done")
    }

}