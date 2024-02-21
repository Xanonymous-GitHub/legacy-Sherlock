package uk.ac.warwick.dcs.sherlock.module.web.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.AccountRepository

/**
 * The custom user details service which fetches the account and
 * their roles from the database from the authentication session
 * information
 */
@Service("userDetailsService")
class CustomUserDetailsService : UserDetailsService {
    //All @Autowired variables are automatically loaded by Spring
    @Autowired
    private val accountRepository: AccountRepository? = null

    /**
     * Searches for the account using the email, loads the roles associated
     * to that account. It then creates a "user" object using the email,
     * encoded password and roles for the authentication manager
     *
     * @param email the email of the user to find
     *
     * @return the new "user" object
     *
     * @throws UsernameNotFoundException if the email was not found in the database
     */
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val account = accountRepository!!.findByEmail(email) ?: throw UsernameNotFoundException("Account not found.")

        val roles: MutableSet<GrantedAuthority> = HashSet()
        for (role in account.roles) {
            roles.add(SimpleGrantedAuthority(role.name))
        }

        return User(account.email, account.password, roles)
    }
}
