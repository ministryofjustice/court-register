package uk.gov.justice.digital.hmpps.courtregister.config

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityUserContext {
  val authentication: AuthAwareAuthenticationToken?
    get() = with(SecurityContextHolder.getContext().authentication) {
      when (this) {
        is AuthAwareAuthenticationToken -> this
        else -> null
      }
    }

  val principal: String
    get() = authentication?.principal ?: "unknown"
}
