package es.unizar.mii.tmdad.chatapp.controller

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.RequestMapping

import org.springframework.web.bind.annotation.RestController


@RestController
class CsrfController {
    @RequestMapping("/csrf")
    fun csrf(token: CsrfToken): CsrfToken {
        return token
    }
}