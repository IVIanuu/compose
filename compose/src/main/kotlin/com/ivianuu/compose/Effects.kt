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

import androidx.compose.Ambient
import androidx.compose.CommitScope
import androidx.compose.Composer
import androidx.compose.Effect
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient
import com.ivianuu.compose.internal.JoinedKey
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.generateKey

fun <T> ComponentComposition.memo(calculation: () -> T) =
    androidx.compose.memo(calculation = calculation).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.memo(vararg inputs: Any?, calculation: () -> T) =
    androidx.compose.memo(inputs = *inputs, calculation = calculation).resolve(
        composer,
        uniqueKey()
    )

fun ComponentComposition.onActive(
    callback: CommitScope.() -> Unit
) = androidx.compose.onActive(callback = callback).resolve(
    composer,
    uniqueKey()
)

fun ComponentComposition.onActive(
    vararg inputs: Any?,
    callback: CommitScope.() -> Unit
) {
    key(*inputs) { onActive(callback) }
}

fun ComponentComposition.onDispose(
    callback: () -> Unit
) = androidx.compose.onDispose(callback = callback).resolve(
    composer,
    uniqueKey()
)

fun ComponentComposition.onDispose(
    vararg inputs: Any?,
    callback: () -> Unit
) {
    key(*inputs) { onDispose(callback) }
}

fun ComponentComposition.onCommit(
    callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(callback = callback).resolve(
        composer,
        uniqueKey()
    )

fun ComponentComposition.onCommit(
    vararg inputs: Any?,
    callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(inputs = *inputs, callback = callback).resolve(
        composer,
        uniqueKey()
    )

fun ComponentComposition.onPreCommit(
    callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(callback = callback).resolve(
        composer,
        uniqueKey()
    )

fun ComponentComposition.onPreCommit(
    vararg inputs: Any?,
    callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(inputs = *inputs, callback = callback).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.state(init: () -> T) =
    androidx.compose.state(init = init).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.stateFor(vararg inputs: Any?, init: () -> T) =
    androidx.compose.stateFor(inputs = *inputs, init = init).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.model(init: () -> T) =
    androidx.compose.modelFor(init = init).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.modelFor(vararg inputs: Any?, init: () -> T) =
    androidx.compose.modelFor(inputs = *inputs, init = init).resolve(
        composer,
        uniqueKey()
    )

fun <T> ComponentComposition.ambient(key: Ambient<T>) =
    androidx.compose.ambient(key = key).resolve(
        composer,
        uniqueKey()
    )

@PublishedApi
internal val invocation = Any()

fun ComponentComposition.distinct(
    vararg inputs: Any?,
    block: ComponentComposition.() -> Unit
) = with(composer) {
    startGroup(uniqueKey())

    if (arrayChanged(inputs)) {
        startGroup(invocation)
        block()
        endGroup()
    } else {
        skipCurrentGroup()
    }

    endGroup()
}

@PublishedApi
internal fun ComponentComposition.arrayChanged(inputs: Array<out Any?>) = with(composer) {
    return@with if ((nextSlot() as? Array<out Any?>)?.let { !it.contentEquals(inputs) } ?: true || inserting) {
        updateValue(inputs)
        true
    } else {
        skipValue()
        false
    }
}

fun ComponentComposition.static(
    block: ComponentComposition.() -> Unit
) = with(composer) {
    startGroup(uniqueKey())

    if (inserting) {
        startGroup(invocation)
        block()
        endGroup()
    } else {
        skipCurrentGroup()
    }

    endGroup()
}

fun ComponentComposition.scope(block: ComponentComposition.() -> Unit) = with(composer) {
    val key = uniqueKey()
    composer.startGroup(key)
    composer.startJoin(key, false) { block() }
    block()
    composer.doneJoin(false)
    composer.endGroup()
}

fun <T> ComponentComposition.key(
    block: ComponentComposition.() -> T
) = key(key = uniqueKey(), block = block)

fun <T> ComponentComposition.key(
    key: Any,
    block: ComponentComposition.() -> T
): T = with(composer) {
    checkIsComposing()
    val environment = ambient(ComponentEnvironmentAmbient)
    environment.pushGroupKey(key)
    startGroup(key)
    val result = block()
    endGroup()
    environment.popGroupKey()
    return@with result
}

fun <T> ComponentComposition.key(
    vararg inputs: Any?,
    block: ComponentComposition.() -> T
) = key(key = uniqueKey(), inputs = *inputs, block = block)

fun <T> ComponentComposition.key(
    key: Any,
    vararg inputs: Any?,
    block: ComponentComposition.() -> T
): T {
    val inputsKey = inputs.reduce { acc, any -> JoinedKey(acc, any) }
    val finalKey = if (inputsKey != null) JoinedKey(key, inputsKey) else key
    return key(finalKey, block)
}

@PublishedApi
internal fun <T> Effect<T>.resolve(composer: Composer<Component<*>>, key: Any): T {
    composer.checkIsComposing()
    return resolve(composer, key.hashCode())
}

fun ComponentComposition.uniqueKey(): Any = generateKey()