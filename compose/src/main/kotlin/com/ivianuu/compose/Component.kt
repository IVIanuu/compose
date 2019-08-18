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

import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.internal.ViewUpdater
import com.ivianuu.compose.internal.component
import com.ivianuu.compose.internal.ensureLayoutParams
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.internal.stackTrace
import com.ivianuu.compose.internal.tagKey
import kotlin.properties.Delegates

class Component<T : View> {

    var viewType: Any by Delegates.notNull()
        internal set

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    private var _parent: Component<*>? = null

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children
    val visibleChildren: List<Component<*>> get() = children.filterNot { it.hidden }

    val boundViews: Set<T> get() = _boundViews
    private val _boundViews = mutableSetOf<T>()

    private var createViewBlock: ((ViewGroup) -> T)? = null
    private var bindViewBlocks: MutableList<(T) -> Unit>? = null
    private var unbindViewBlocks: MutableList<(T) -> Unit>? = null

    private var layoutChildViewsBlock: ((T) -> Unit)? = null
    private var bindChildViewsBlock: ((T) -> Unit)? = null
    private var unbindChildViewsBlock: ((T) -> Unit)? = null

    internal var viewUpdater: ViewUpdater<T>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var isPush = true
    internal var hidden = false
        internal set
    internal var byId = false
        internal set
    internal var generation = 0

    fun updateChildren(newChildren: List<Component<*>>) {
        log { "lifecycle: $key -> update children new ${newChildren.map { it.key }} old ${_children.map { it.key }}" }

        _children
            .filter { it !in newChildren }
            .forEach { it._parent = null }

        newChildren
            .filter { it !in _children }
            .forEach { it._parent = this }

        _children.clear()
        _children += newChildren

        _boundViews.forEach {
            layoutChildViews(it)
            bindChildViews(it)
        }
    }

    fun createView(container: ViewGroup): T {
        check(createViewBlock != null)
        log { "lifecycle: $key -> create view $container" }
        val view = createViewBlock!!(container)
        view.ensureLayoutParams(container)
        return view
    }

    fun bindView(view: T) {
        log { "lifecycle: $key -> bind view $view" }

        val newView = view.component != this

        _boundViews += view
        view.component = this

        bindViewBlocks?.forEach { it(view) }

        if (newView) {
            log { "updater: $key update new view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(
                ViewUpdater.Type.Init,
                ViewUpdater.Type.Update,
                ViewUpdater.Type.Value
            )
                ?.forEach { it(view) }
        } else if (view.generation != generation) {
            log { "updater: $key update view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(ViewUpdater.Type.Update, ViewUpdater.Type.Value)
                ?.forEach { it(view) }
        } else {
            log { "updater: $key skip update $generation" }
            viewUpdater?.getBlocks(ViewUpdater.Type.Update)
                ?.forEach { it(view) }
        }
    }

    fun unbindView(view: T) {
        log { "lifecycle: $key -> unbind view $view" }
        unbindViewBlocks?.forEach { it(view) }
        view.generation = null
        view.component = null
        _boundViews -= view
    }

    fun layoutChildViews(view: T) {
        log { "lifecycle: $key -> layout child views $view block ? $layoutChildViewsBlock" }

        if (layoutChildViewsBlock != null) {
            layoutChildViewsBlock!!.invoke(view)
        } else {
            if (view !is ViewGroup) return
            view.getViewManager().update(
                visibleChildren,
                visibleChildren.lastOrNull()?.isPush ?: true
            )
        }
    }

    fun bindChildViews(view: T) {
        log { "lifecycle: $key -> bind child views $view block ? $layoutChildViewsBlock" }

        if (bindChildViewsBlock != null) {
            bindChildViewsBlock!!.invoke(view)
        } else {
            if (view !is ViewGroup) return
            view.getViewManager().viewsByChild.forEach { (component, view) ->
                (component as Component<View>).bindView(view)
            }
        }
    }

    fun unbindChildViews(view: T) {
        log { "lifecycle: $key -> unbind child views $view block ? $layoutChildViewsBlock" }

        if (key.toString().contains("36")) {
            stackTrace { key.toString() }
        }

        if (unbindChildViewsBlock != null) {
            unbindChildViewsBlock!!.invoke(view)
        } else {
            if (view !is ViewGroup) return
            view.getViewManager().viewsByChild.forEach { (component, view) ->
                (component as Component<View>).unbindView(view)
            }
        }
    }

    @PublishedApi
    internal fun onCreateView(callback: (ViewGroup) -> T): () -> Unit {
        createViewBlock = callback
        return { createViewBlock = null }
    }

    @PublishedApi
    internal fun onBindView(callback: (T) -> Unit): () -> Unit {
        if (bindViewBlocks == null) bindViewBlocks = mutableListOf()
        bindViewBlocks!! += callback
        return { bindViewBlocks!! -= callback }
    }

    @PublishedApi
    internal fun onUnbindView(callback: (T) -> Unit): () -> Unit {
        if (unbindViewBlocks == null) unbindViewBlocks = mutableListOf()
        unbindViewBlocks!! += callback
        return { unbindViewBlocks!! -= callback }
    }

    @PublishedApi
    internal fun onLayoutChildViews(callback: (T) -> Unit): () -> Unit {
        layoutChildViewsBlock = callback
        return { layoutChildViewsBlock = null }
    }

    @PublishedApi
    internal fun onBindChildViews(callback: (T) -> Unit): () -> Unit {
        bindChildViewsBlock = callback
        return { bindChildViewsBlock = null }
    }

    @PublishedApi
    internal fun onUnbindChildViews(callback: (T) -> Unit): () -> Unit {
        unbindChildViewsBlock = callback
        return { unbindChildViewsBlock = null }
    }
}

private val generationKey = tagKey("generation")

var View.generation: Int?
    get() = getTag(generationKey) as? Int
    set(value) {
        setTag(generationKey, value)
    }