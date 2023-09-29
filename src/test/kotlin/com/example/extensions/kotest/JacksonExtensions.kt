package com.example.extensions.kotest

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.json.*

fun JsonNode.shouldEqualJson(expected: JsonNode, options: CompareJsonOptions) {
    this.toPrettyString().shouldEqualJson(expected.toPrettyString(), options)
}

fun JsonNode.shouldContainJsonUnordered(expected: JsonNode) {
    this.shouldEqualJson(
        expected,
        CompareJsonOptions(propertyOrder = PropertyOrder.Lenient, arrayOrder = ArrayOrder.Lenient, fieldComparison = FieldComparison.Lenient)
    )
}