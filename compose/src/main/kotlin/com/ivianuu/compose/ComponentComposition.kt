/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose

import androidx.compose.EffectsDsl
import java.util.*

@Suppress("UNCHECKED_CAST")
@EffectsDsl
class ComponentComposition(val composer: ComponentComposer) {

    private val keysStack = Stack<MutableList<Any>>()
    private var keys = mutableListOf<Any>()

    private val groupKeyStack = Stack<Any?>()
    private var groupKey: Any? = null

    fun joinKey(left: Any, right: Any?): Any = composer.joinKey(left, right)

    fun <T : Component<*>> emit(
        key: Any,
        ctor: () -> T,
        update: (T.() -> Unit)? = null
    ) = with(composer) {
        val finalKey = joinKeyIfNeeded(key, groupKey)

        check(finalKey !in keys) {
            "Duplicated key $finalKey"
        }

        keys.add(finalKey)

        keysStack.push(keys)
        keys = mutableListOf()
        startNode(finalKey)

        log { "emit $finalKey inserting ? $inserting" }
        val node = if (inserting) {
            ctor().also { emitNode(it) }
        } else {
            useNode() as T
        }

        node._key = finalKey

        // todo remove
        node.inChangeHandler = ambient(InChangeHandlerAmbient)
        node.outChangeHandler = ambient(OutChangeHandlerAmbient)
        node.wasPush = ambient(TransitionHintsAmbient)

        update?.let { node.it() }
        node.update()

        endNode()
        keys = keysStack.pop()
    }

    fun key(
        key: Any,
        children: ComponentComposition.() -> Unit
    ) = with(composer) {
        val previousGroupKey = groupKey
        groupKeyStack.push(groupKey)
        val finalKey = joinKeyIfNeeded(key, previousGroupKey)
        groupKey = finalKey

        startGroup(finalKey)
        keysStack.push(keys)
        keys = mutableListOf()
        children()
        keys = keysStack.pop()
        endGroup()

        groupKey = groupKeyStack.pop()
    }

    inline fun key(noinline children: ComponentComposition.() -> Unit) =
        key(sourceLocation(), children)

}

private data class JoinedKey(val left: Any, val right: Any) {
    override fun toString(): String = "($left,$right)"
}

private fun joinKeyIfNeeded(key: Any, groupKey: Any?): Any {
    return if (groupKey != null) {
        JoinedKey(key, groupKey)
    } else {
        key
    }
}