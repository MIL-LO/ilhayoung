package com.millo.ilhayoung.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "헬스체크")
@RestController
class HealthCheck {
    @GetMapping("/health")
    fun check(): String {
        return "check"
    }
}