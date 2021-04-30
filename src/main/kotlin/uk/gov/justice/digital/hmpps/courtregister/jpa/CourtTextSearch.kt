package uk.gov.justice.digital.hmpps.courtregister.jpa

import com.vladmihalcea.hibernate.type.search.PostgreSQLTSVectorType
import org.hibernate.annotations.Immutable
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.spi.MetadataBuilderContributor
import org.hibernate.dialect.function.SQLFunctionTemplate
import org.hibernate.type.BooleanType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Immutable
@Table(name = "court_text_search")
data class CourtTextSearch(
  @Id
  val id: String,
  val textSearchVector: PostgreSQLTSVectorType,
)

class TextSearchSqlFunctionTemplate : MetadataBuilderContributor {
  override fun contribute(metadataBuilder: MetadataBuilder) {
    metadataBuilder.applySqlFunction("search_court_text", SQLFunctionTemplate(BooleanType.INSTANCE, "textSearchVector @@ plainto_tsquery(?1)"))
  }
}
