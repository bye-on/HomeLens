package com.homelens.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.homelens.filter.security.JWTVerificationFilter;
import com.homelens.filter.security.SecurityExceptionHandlingFilter;
import com.homelens.jwt.JwtFilter;
import com.homelens.jwt.LoginFilter;
import com.homelens.util.JwtUtil;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

//	private final AuthenticationConfiguration authenticationConfiguration;
	private final JwtUtil jwtUtil;

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManger(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
			@Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfig,
			LoginFilter loginFilter,
			JWTVerificationFilter jwtVerifyFilter,
			SecurityExceptionHandlingFilter exceptionFilter) throws Exception {
		http.csrf((auth) -> auth.disable())
		.cors(cors-> cors.configurationSource(corsConfig));

		http.formLogin((auth) -> auth.disable());

		http.httpBasic((auth) -> auth.disable());

		http.authorizeHttpRequests((auth) -> auth.requestMatchers("/login", "/", "/join", "/**").permitAll()
				.requestMatchers("api/v1/admin/**", "api/v1/board/write/**", "api/v1/board/edit/**", "api/v1/board/delete/**").hasRole("ADMIN").anyRequest().authenticated());
//		http.authorizeHttpRequests((auth) -> auth.anyRequest().permitAll());
		
//		http.addFilterAt(new JwtFilter(jwtUtil), LoginFilter.class);

		http.addFilterAt(loginFilter,UsernamePasswordAuthenticationFilter.class)
		.addFilterBefore(jwtVerifyFilter, LoginFilter.class)
    	.addFilterBefore(exceptionFilter, JWTVerificationFilter.class);

		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
	
	@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:5174"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/member/checkEmail", configuration);

        return source;
    }
}
