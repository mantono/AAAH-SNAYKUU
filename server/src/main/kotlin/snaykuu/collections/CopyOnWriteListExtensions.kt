package snaykuu.collections

import java.util.*

fun <T> List<T>.append(other: T): List<T> = modify { addLast(other) }
fun <T> List<T>.append(other: List<T>): List<T> = modify { addAll(other) }
fun <T> List<T>.removeLast(): List<T> = modify { removeLast() }

operator fun <T> List<T>.plus(other: T): List<T> = modify { addLast(other) }
operator fun <T> List<T>.minus(other: T): List<T> = modify { remove(other) }

inline fun <T, V> List<T>.modify(change: LinkedList<T>.() -> V): List<T> {
    val new = LinkedList(this)
    new.change()
    return new
}