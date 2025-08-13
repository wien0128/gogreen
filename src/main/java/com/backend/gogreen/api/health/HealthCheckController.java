package com.backend.gogreen.api.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "AWS EC2 헬스체크", description = "AWS EC2 헬스체크용 컨트롤러")
public class HealthCheckController {

    @Operation(
            summary = "AWS EC2 헬스체크용",
            description = "AWS EC2 헬스체크용 엔드포인트입니다."
    )
    @GetMapping("health")
    public String healthCheck() {
        return "OK";
    }
}
