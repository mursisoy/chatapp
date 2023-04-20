package es.unizar.mii.tmdad.tahc.config


import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@ConfigurationPropertiesScan
@EnableWebSecurity
class SecurityConfiguration(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationProvider: AuthenticationProvider,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf().ignoringRequestMatchers(toH2Console())
            .disable()
            .headers().frameOptions().sameOrigin()
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/v1/auth/**")
            .permitAll()
            .requestMatchers(toH2Console())
            .permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        http.cors()
            .configurationSource(corsConfigurationSource())
        return http.orBuild
    }

    private fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL)
        corsConfiguration.addAllowedHeader((CorsConfiguration.ALL))

        corsConfiguration.addAllowedOrigin("http://localhost:5173")
        corsConfiguration.allowCredentials = true

        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)

        return corsConfigurationSource
    }

//    @Bean
//    @ConditionalOnProperty(name = ["spring.h2.console.enabled"], havingValue="true")
//    fun h2ConsoleSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http
//            .authorizeHttpRequests()
//            .requestMatchers(toH2Console())
//            .permitAll()
////            .and()
////            .csrf().ignoringRequestMatchers(toH2Console()).disable()
////            .and().headers().frameOptions().sameOrigin();
//        return http.orBuild
//    }
}