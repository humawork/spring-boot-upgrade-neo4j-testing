package com.example.demo

import com.example.demo.base.BaseTests
import com.example.extensions.kotest.shouldContainJsonUnordered
import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpStatus.OK
import java.util.UUID

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProjectionTests() : BaseTests() {

    @Test
    fun listOrganizationUsers() {
        val organizationId = "/organization".POST(json { "name" to "MyOrg" }).asClue { response ->
            response.statusCode shouldBe OK
            response.body!!.path("id").toUUID()
        } // 1 cypher request

        val olaId = createUser(organizationId, "Ola", "Nordmann") // 6 cypher requests
        val kariId = createUser(organizationId, "Kari", "Nordmann", supervisor = olaId) // 15 cypher requests

        val users = listUsers(organizationId)
        users.shouldContainJsonUnordered(
            array(
                json {
                    "id" to olaId.toString()
                    "givenName" to "Ola"
                    "familyName" to "Nordmann"
                },
                json {
                    "id" to kariId.toString()
                    "givenName" to "Kari"
                    "familyName" to "Nordmann"
                    "supervisedBy" to json {
                        "id" to olaId.toString()
                        "givenName" to "Ola"
                        "familyName" to "Nordmann"
                    }
                }
            )
        )
    }

    @Test
    fun listOrganizationUsersWithTemplate() {
        val organizationId = "/organization".POST(json { "name" to "MyOrg" }).asClue { response ->
            response.statusCode shouldBe OK
            response.body!!.path("id").toUUID()
        } // 1 cypher request

        val olaId = createUser(organizationId, "Ola", "Nordmann") // 6 cypher requests
        val kariId = createUser(organizationId, "Kari", "Nordmann", supervisor = olaId) // 15 cypher requests

        val templateUsers = listUsersWithTemplate(organizationId)
        templateUsers.shouldContainJsonUnordered(
            array(
                json {
                    "id" to olaId.toString()
                    "givenName" to "Ola"
                    "familyName" to "Nordmann"
                },
                json {
                    "id" to kariId.toString()
                    "givenName" to "Kari"
                    "familyName" to "Nordmann"
                    "supervisedBy" to json {
                        "id" to olaId.toString()
                        "givenName" to "Ola"
                        "familyName" to "Nordmann"
                    }
                }
            )
        )
    }

    private fun listUsers(organizationId: UUID): JsonNode {
        return "/organization/$organizationId/users".GET().asClue { response ->
            response.statusCode shouldBe OK
            response.body!!
        }
    }

    private fun listUsersWithTemplate(organizationId: UUID): JsonNode {
        return "/organization/$organizationId/users-template".GET().asClue { response ->
            response.statusCode shouldBe OK
            response.body!!
        }
    }
}
