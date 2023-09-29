package com.example.demo.base

import com.example.demo.json
import com.example.demo.toUUID
import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import java.util.UUID
import kotlin.reflect.KClass

open class BaseTests {
    private val restTemplate = TestRestTemplate()

    companion object {

        @JvmStatic
        @Container
        val neo4jContainer: Neo4jContainer<*> = Neo4jContainer(DockerImageName.parse("neo4j:4.4"))
            .withoutAuthentication()
            .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            if (!neo4jContainer.isRunning) {
                neo4jContainer.start()
            }
            registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl)
            registry.add("spring.neo4j.authentication.enabled") { "false" }
            registry.add("spring.neo4j.security.encryption.enabled") { "false" }
        }
    }

    @LocalServerPort
    private val port: Int = 0
    private val urlBase by lazy { "http://localhost:$port" }

    protected fun createUser(organizationId: UUID, givenName: String, familyName: String, supervisor: UUID? = null): UUID {
        return "/organization/$organizationId/users".POST(
            json {
                "givenName" to givenName
                "familyName" to familyName
                "supervisorId" to supervisor
            }
        ).asClue { response ->
            response.statusCode shouldBe HttpStatus.OK
            response.body!!.path("id").toUUID()
        }
    }

    protected fun findUser(organizationId: UUID, userId: UUID): JsonNode {
        return "/organization/$organizationId/users/$userId".GET().asClue { response ->
            response.statusCode shouldBe HttpStatus.OK
            response.body!!
        }
    }

    @Suppress("TestFunctionName")
    protected fun String.GET() = request(
        method = HttpMethod.GET,
        path = this,
        responseType = JsonNode::class,
    )

    @Suppress("TestFunctionName")
    protected fun String.PUT(body: JsonNode? = null) = request(
        method = HttpMethod.PUT,
        path = this,
        input = body,
        responseType = JsonNode::class,
    )

    @Suppress("TestFunctionName")
    protected fun String.POST(body: JsonNode? = null) = request(
        method = HttpMethod.POST,
        path = this,
        input = body,
        responseType = JsonNode::class,
    )

    @Suppress("TestFunctionName")
    protected fun String.PATCH(body: JsonNode? = null) = request(
        method = HttpMethod.PATCH,
        path = this,
        input = body,
        responseType = JsonNode::class,
    )

    private fun <T : Any> request(
        method: HttpMethod,
        path: String,
        input: JsonNode? = null,
        responseType: KClass<T>,
        contentType: MediaType = MediaType.APPLICATION_JSON,
    ): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.contentType = contentType
        return restTemplate.exchange("$urlBase$path", method, HttpEntity(input, headers), responseType.java)
    }
}