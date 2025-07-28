package com.freewheelin.pulley.common.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .addServersItem(
                Server()
                    .url("http://localhost:8080")
                    .description("로컬 개발 서버")
            )
            .info(
                Info()
                    .title("Pulley API")
                    .description("""
                        학습지 관리 시스템 API 문서
                        
                        ## 🔐 인증 방법
                        모든 API 요청 시 **X-User-Id** 헤더에 사용자 ID를 포함해야 합니다.
                        
                        **사용법:**
                        1. 우측 상단의 🔒 Authorize 버튼 클릭
                        2. X-User-Id 필드에 사용자 ID 입력 (예: 1)
                        3. Authorize 버튼 클릭
                        
                        ## 👥 테스트용 사용자 정보
                        ### 선생님 계정 (선생님 전용 API 테스트용)
                        - **ID: 1** - 김선생 (teacher1)
                        - **ID: 2** - 이선생 (teacher2) 
                        - **ID: 3** - 박선생 (teacher3)
                        
                        ### 학생 계정 (학생 API 테스트용)
                        - **ID: 4** - 홍길동 (student1)
                        - **ID: 5** - 김철수 (student2)
                        - **ID: 6** - 이영희 (student3)
                        - **ID: 7** - 박민수 (student4)
                        - **ID: 8** - 최지수 (student5)
                        
                        ## 📚 테스트용 데이터
                        ### 문제 ID 예시
                        - **1001~1005**: uc1580 유닛코드, 난이도 2, 객관식
                        - **1051~1053**: uc1576 유닛코드, 난이도 2, 객관식
                        - **1231~1233**: uc1523 유닛코드, 난이도 2, 주관식
                        
                        ### 유닛 코드 예시
                        - **uc1580**: 표본추출 - 단순랜덤추출
                        - **uc1576**: 중심 경향 척도 - 자료의 평균, 평균절대편차, 최빈값
                        - **uc1523**: 확률변수의 기대값
                        
                        ## 빠른 테스트 가이드
                        1. **선생님으로 로그인**: X-User-Id = 1
                        2. **문제 검색**: `/problems?totalCount=5&unitCodeList=uc1580,uc1576&level=HIGH&problemType=ALL`
                        3. **학습지 생성**: POST `/piece` with `{"title": "테스트 학습지", "problemIds": [1001, 1002, 1051]}`
                        4. **학습지 출제**: POST `/piece/1` with `{"studentIds": [4, 5]}`
                        5. **학생으로 로그인**: X-User-Id = 4
                        6. **답안 제출**: PUT `/piece/1/score` with `{"answers": [{"problemId": 1001, "answer": "1"}]}`
                    """.trimIndent())
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Pulley Team")
                            .email("support@pulley.com")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("userIdAuth"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "userIdAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.HEADER)
                            .name("X-User-Id")
                            .description("사용자 ID를 입력하세요 (선생님: 1-3, 학생: 4-8)")
                    )
            )
    }
}