package snaykuu.reflection

import org.reflections.Reflections
import snaykuu.gameLogic.Brain

fun getBrains(): Set<Class<out Brain>> {
    return Reflections("").getSubTypesOf(Brain::class.java).asSequence()
        .filterNot { it.isAnonymousClass }
        .filterNot { it.name.endsWith("BrainDead") }
        .toSet()
}