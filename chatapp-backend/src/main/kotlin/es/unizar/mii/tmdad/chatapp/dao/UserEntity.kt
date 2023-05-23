package es.unizar.mii.tmdad.chatapp.dao

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Entity
class UserEntity(

    @Id
    @Column(name="id", unique = true)
    val id: UUID = UUID.randomUUID(),

    @Column(name="username",unique = true)
    @JvmField
    val username: String,

    @Column
    @JvmField
    val password: String = "",

    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER

) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(this.role)
    }

    override fun getPassword(): String {
        return password
    }
    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}