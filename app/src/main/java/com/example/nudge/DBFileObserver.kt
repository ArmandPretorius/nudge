package com.example.nudge

import android.os.FileObserver.CREATE
import java.util.*

const val DB_NAME = "ToDoList"
const val DB_VERSION = 1
const val COL_ID = "id"
const val COL_CREATED_AT = "createdAt"

const val TABLE_TODO_ITEM = "ToDoItem"
const val COL_TODO_ID = "toDoId"
const val COL_ITEM_NAME = "itemName"
const val COL_IS_COLPLETED = "isCompleted"

const val INTENT_TODO_ID = "TodoId"
const val INTENT_TODO_NAME = "TodoName"