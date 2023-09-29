package com.example.demo.controllers

import com.example.demo.model.UserProjection
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
@RequestMapping("/organization/{organizationId}/users-template")
class UserTemplateController(
    private val neo4jTemplate: Neo4jTemplate
) {


    @GetMapping("")
    fun list(
        @PathVariable organizationId: String,
    ): List<UserProjection> {
        return neo4jTemplate.findAll(
            """
                MATCH (u:User)
                OPTIONAL MATCH (u)-[sb:SUPERVISED_BY]->(s:User)
                RETURN 
                    u.id AS id,
                    u.givenName AS givenName,
                    u.familyName AS familyName,
                    collect(sb),
                    collect(s) AS supervisedBy
            """,
            UserProjection::class.java,
        )
    }
}