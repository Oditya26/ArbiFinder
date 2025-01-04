package com.example.arbitrade.gemini

import com.google.gson.annotations.SerializedName

//Request
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)


//Response
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata,
    val modelVersion: String
)

data class Candidate(
    val content: Content,
    val finishReason: String,
    val avgLogprobs: Double
)

data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)

