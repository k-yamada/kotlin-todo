package model

import org.jetbrains.exposed.dao.IntIdTable

object Todos: IntIdTable() {
    val title = varchar("title", 50)
    val description = varchar("description", 1000)
}



