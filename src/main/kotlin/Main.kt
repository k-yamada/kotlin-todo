import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Todo
import model.TodoData
import model.Todos
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    setupDatabase()
    embeddedServer(Netty, 8080, watchPaths = listOf("BlogAppKt"), module = Application::module).start()
}

data class TodoParameter(val title: String, val description: String)

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
        }
    }
    install(Routing) {
        // curl localhost:8080/todos
        get("/todos") {
            var result = emptyList<TodoData>()
            transaction {
                val todos = Todo.all()
                result = todos.map { it.toData() }
            }
            call.respond(mapOf("todos" to result))
        }

        // curl -X POST -H "Content-Type: application/json" -d '{"title":"hoge", "description":"fuga"}' localhost:8080/todos
        post("/todos") {
            val parameter = call.receive<TodoParameter>()
            var todo: Todo? = null
            transaction {
                todo = Todo.new {
                    title = parameter.title
                    description = parameter.description
                }
            }
            call.respond(todo?.toData() ?: "")
        }

//        get("/todos/:id") {
//            model.Todo.findById()
//        }


    }
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

