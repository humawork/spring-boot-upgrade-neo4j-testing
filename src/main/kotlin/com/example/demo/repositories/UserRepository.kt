package com.example.demo.repositories

import com.example.demo.model.User
import com.example.demo.model.UserProjection
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : Neo4jRepository<User, String> {
    @Query(
        """
        MATCH (:Organization{id: ${'$'}organizationId})<-[:BELONGS_TO]-(u:User)
        OPTIONAL MATCH (u)-[sb:SUPERVISED_BY]->(s:User)-[:BELONGS_TO]->(o)
        RETURN u, collect(sb), collect(s) AS supervisedBy
        """
    )
    fun findAllProjected(organizationId: String): List<UserProjection>
}
