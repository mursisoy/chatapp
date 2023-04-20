package es.unizar.mii.tmdad.tahc.entity

import org.springframework.security.core.GrantedAuthority

enum class Role : GrantedAuthority {
    USER {
        override fun getAuthority(): String {
            return this.name
        }
    },
    ADMIN {
        override fun getAuthority(): String {
            return this.name
        }
    }
}
