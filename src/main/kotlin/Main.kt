import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
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
        get("/todos") {
            var result = emptyList<TodoData>()
            transaction {
                val todos = Todo.all()
                result = todos.map { it.toData() }
            }
            call.respond(mapOf("todos" to result))
        }

        post("/todos") {
            val parameter = call.receive<TodoParameter>()
            var todo: Todo? = null
            transaction {
                todo = Todo.new {
                    title = parameter.title
                    description = parameter.description
                }
            }
            if (todo == null) {
                call.respond(HttpStatusCode.InternalServerError)
                return@post
            }
            call.respond(todo!!.toData())
        }

        get("/todos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            var todo: Todo? = null
            transaction {
                todo = Todo.findById(id)
            }
            if (todo == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(todo!!.toData())
        }

        delete("/todos/{id}") {
            val id = call.parameters["id"]?.toInt() ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            var statusCode = HttpStatusCode.OK
            transaction {
                val todo = Todo.findById(id)
                if (todo == null) {
                    statusCode = HttpStatusCode.NotFound
                    return@transaction
                }
                todo.delete()
            }
            call.respond(statusCode)
        }
    }
}

private fun setupDatabase() {
    // MySQLの `todo` データベースに接続
    Database.connect("jdbc:mysql://localhost/todo", "com.mysql.jdbc.Driver", "root", "")

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        // テーブルを作成
        SchemaUtils.create(Todos)
    }
}

