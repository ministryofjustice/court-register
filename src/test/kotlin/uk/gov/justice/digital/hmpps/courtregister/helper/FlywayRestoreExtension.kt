package uk.gov.justice.digital.hmpps.courtregister.helper

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension

class FlywayRestoreExtension : AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    var flyway: Flyway? = null
  }

  override fun beforeEach(context: ExtensionContext) {
    val applicationContext = SpringExtension.getApplicationContext(context)
    if (applicationContext.containsBean("flyway")) {
      flyway = applicationContext.getBean(Flyway::class.java)
    }
  }

  override fun afterAll(context: ExtensionContext) {
    flyway?.clean()
    flyway?.migrate()
  }
}
