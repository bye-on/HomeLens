package com.homelens.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

//Swagger-UI 확인
//http://localhost/swagger-ui/index.html

@OpenAPIDefinition(
		info = @Info(
				title = "HomeLens 명세서",
				description = "<h3>HomeLens API Reference for Developers</h3>Swagger를 이용한 VUE API<br>",
				version = "v1"
		)
)

@Configuration
public class SwaggerConfig {
	
	@Bean
	public GroupedOpenApi allApi() {
		return GroupedOpenApi.builder().group("all").pathsToMatch("/**").build();
	}
	
	@Bean
	public GroupedOpenApi boardApi() {
		return GroupedOpenApi.builder().group("board").pathsToMatch("/board/**").build();
	}
	
	@Bean
	public GroupedOpenApi userApi() {
		return GroupedOpenApi.builder().group("user").pathsToMatch("/api/v1/user/**").build();
	}

	@Bean
	public GroupedOpenApi authApi() {
		return GroupedOpenApi.builder().group("auth").pathsToMatch("/api/v1/auth/**").build();
	}
}
