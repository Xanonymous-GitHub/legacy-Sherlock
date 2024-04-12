package uk.ac.warwick.dcs.sherlock.module.web.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import uk.ac.warwick.dcs.sherlock.module.web.configuration.properties.SecurityProperties
import uk.ac.warwick.dcs.sherlock.module.web.configuration.properties.SetupProperties
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Role
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.RoleRepository
import java.security.SecureRandom
import java.util.*

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val accountRepository: AccountRepository,
    private val roleRepository: RoleRepository,
    private val environment: Environment,
    private val setupProperties: SetupProperties,
    private val securityProperties: SecurityProperties,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val userDetailsService: UserDetailsService
) {
    companion object {
        fun getLocalEmail(): String = "local@dcs-sherlock.github.io"
        fun getLocalPassword(): String = "local_password"
        fun generateRandomPassword(): String {
            val random = SecureRandom()
            val bytes = ByteArray(12)
            random.nextBytes(bytes)
            return Base64.getEncoder().encodeToString(bytes)
        }
    }

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        if (environment.activeProfiles.contains("client")) {
            var account = accountRepository.findByEmail(getLocalEmail())
            if (account == null) {
                account = Account(
                    getLocalEmail(),
                    passwordEncoder.encode(getLocalPassword()),
                    "Local User"
                ).also { accountRepository.save(it) }
                roleRepository.save(Role("USER", account))
                roleRepository.save(Role("LOCAL_USER", account))
            }
        } else {
            if (accountRepository.count() == 0L) {
                val account = Account(
                    setupProperties.email,
                    passwordEncoder.encode(setupProperties.password),
                    setupProperties.name
                ).also { accountRepository.save(it) }
                roleRepository.save(Role("USER", account))
                roleRepository.save(Role("ADMIN", account))
            }
        }
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests {
            it.requestMatchers("/css/**", "/js/**", "/img/**", "/fonts/**").permitAll()
                .requestMatchers("/", "/terms", "/privacy", "/help/**").permitAll()
                .requestMatchers("/dashboard/**").hasAuthority("USER")
                .requestMatchers("/account/**")
                .hasAuthority(if (environment.activeProfiles.contains("client")) "ADMIN" else "USER")
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
            if (environment.activeProfiles.contains("dev")) {
                it.requestMatchers("/h2-console/**").permitAll()
            }
        }.formLogin {
            it.loginPage("/login")
                .defaultSuccessUrl("/dashboard/index")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        }.logout {
            it.deleteCookies("JSESSIONID")
        }.rememberMe {
            it.key(securityProperties.key)
        }
        if (environment.activeProfiles.contains("dev")) {
            http.let {
                it.headers { config ->
                    config.frameOptions { options -> options.disable() }
                }
            }
        }

        return http.build()
    }
}
