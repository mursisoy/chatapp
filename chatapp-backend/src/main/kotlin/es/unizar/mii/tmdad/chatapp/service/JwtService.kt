package es.unizar.mii.tmdad.chatapp.service

import es.unizar.mii.tmdad.chatapp.dao.Role
import es.unizar.mii.tmdad.chatapp.dao.UserEntity
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.cglib.core.internal.Function
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtService {
    fun extractSubject(token: String): String? {
        return extractClaim(token, Claims::getSubject)
    }

    fun extractUser(token: String): UserDetails? {
        val claims: Claims = extractAllClaims(token)
        return try {
            UserEntity(
                id = UUID.fromString(claims["id"].toString()),
                username = extractClaim(token, Claims::getSubject),
                role = enumValueOf<Role>(claims["role"].toString())
            )
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun <T> extractClaim(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractSubject(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    fun generateToken(user: UserEntity): String {
        return generateToken(
            hashMapOf(
                "id" to user.id,
                "username" to user.username,
                "role" to user.role
            ), user)
    }

    fun generateToken(
        extraClaims: Map<String, Any>,
        user: UserEntity
    ): String {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(user.username)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact()
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .body
    }

    private fun getSignInKey(): SecretKey = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray())

    companion object {
        private const val SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
        private const val ACCESS_TOKEN_VALIDITY_SECONDS  = 365 * 24 * 60 * 4
    }
}
