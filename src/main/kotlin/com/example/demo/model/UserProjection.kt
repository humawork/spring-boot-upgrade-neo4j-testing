package com.example.demo.model

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Relationship

data class UserProjection(
    @Id
    val id: String,
    val givenName: String,
    val familyName: String,

    @Relationship(type = "SUPERVISED_BY")
    val supervisedBy: User? = null
)