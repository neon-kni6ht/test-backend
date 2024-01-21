package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        var author: AuthorEntity? = null
        body.authorId?.let {
                author = AuthorService.read(body.authorId)
        }

        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable.join(AuthorTable, JoinType.LEFT, BudgetTable.author, AuthorTable.id )
                .select { (BudgetTable.year eq param.year) }

            param.authorName?.let {
                query.andWhere {
                    (AuthorTable.fullName.isNotNull()) and (AuthorTable.fullName.lowerCase() like "%${it.toLowerCase()}%")
                }
            }

            query.orderBy(BudgetTable.year to SortOrder.DESC, BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }
            val page = BudgetEntity.wrapRows(query.limit(param.limit, param.offset)).map {
                it.toResponse()
            }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }
            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = page
            )
        }
    }
}