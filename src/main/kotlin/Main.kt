import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    setupDatabase()
}

private fun setupDatabase() {
    // MySQLの「todo」データベースに接続
    Database.connect("jdbc:mysql://localhost/todo", "com.mysql.jdbc.Driver", "root", "")

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        // テーブルを作成
        SchemaUtils.create (Todos)
    }
}