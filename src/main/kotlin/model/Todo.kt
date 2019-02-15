package model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Todo(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Todo>(Todos)

    var title by Todos.title
    var description by Todos.description

    fun toData(): TodoData = TodoData(id.value, title, description)
}