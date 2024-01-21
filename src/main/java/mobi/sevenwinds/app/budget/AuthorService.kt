package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
object AuthorService {
    suspend fun addRecord(body: String): AuthorRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = body
            }

            return@transaction entity.toResponse()
        }
    }
    suspend fun read(id: Int): AuthorEntity? = withContext(Dispatchers.IO) {
        transaction {
            val query = AuthorTable.select(AuthorTable.id eq id).singleOrNull()
            query?.let {
                return@transaction AuthorEntity.wrapRow(query)
            }
            return@transaction null
        }
    }
}