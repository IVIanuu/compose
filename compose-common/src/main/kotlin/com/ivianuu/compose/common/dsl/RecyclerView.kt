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

package com.ivianuu.compose.common.dsl

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.ChildViewController
import com.ivianuu.compose.Component
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.common.flow
import com.ivianuu.compose.init
import com.ivianuu.compose.internal.component
import com.ivianuu.compose.memo
import com.ivianuu.compose.onUnbindView
import com.ivianuu.compose.set
import com.ivianuu.compose.update
import kotlinx.coroutines.flow.Flow

inline fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    flow: Flow<Iterable<T>>,
    crossinline placeholder: ComponentComposition.() -> Unit = {},
    crossinline itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    val items = flow(flow)
    if (items != null) {
        RecyclerView(layoutManager = layoutManager, items = items, itemBuilder = itemBuilder)
    } else {
        placeholder()
    }
}

inline fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    items: Array<T>,
    crossinline itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        items.forEachIndexed { index, item ->
            itemBuilder(index, item)
        }
    }
}

inline fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    items: Iterable<T>,
    crossinline itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        items.forEachIndexed { index, item ->
            itemBuilder(index, item)
        }
    }
}

inline fun ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    itemCount: Int,
    crossinline itemBuilder: ComponentComposition.(Int) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        (0 until itemCount).forEach { index ->
            itemBuilder(index)
        }
    }
}

fun ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    children: ComponentComposition.() -> Unit
) {
    View(childViewController = RecyclerViewChildViewController) {
        val layoutManagerStateHolder = memo { LayoutManagerStateHolder() }

        set(layoutManager) { this.layoutManager = it ?: LinearLayoutManager(context) }

        init { adapter = ComposeRecyclerViewAdapter() }

        update { (adapter as ComposeRecyclerViewAdapter).submitList(component!!.visibleChildren.toList()) }

        update {
            if (layoutManagerStateHolder.state != null) {
                this.layoutManager!!.onRestoreInstanceState(layoutManagerStateHolder.state)
                layoutManagerStateHolder.state = null
            }
        }

        onUnbindView {
            layoutManagerStateHolder.state = it.layoutManager?.onSaveInstanceState()
            it.adapter = null
        } // calls unbindView on all children

        children()
    }
}

fun <T : RecyclerView> ComponentBuilder<T>.adapter(adapter: RecyclerView.Adapter<*>?) {
    set(adapter) { this.adapter = it }
}

fun <T : RecyclerView> ComponentBuilder<T>.layoutManager(layoutManager: RecyclerView.LayoutManager?) {
    set(layoutManager) { this.layoutManager = it }
}

private data class LayoutManagerStateHolder(var state: Parcelable? = null)

private object RecyclerViewChildViewController : ChildViewController<RecyclerView> {
    override fun initChildViews(component: Component<RecyclerView>, view: RecyclerView) {
    }

    override fun updateChildViews(component: Component<RecyclerView>, view: RecyclerView) {
    }

    override fun clearChildViews(component: Component<RecyclerView>, view: RecyclerView) {
    }
}

private class ComposeRecyclerViewAdapter :
    ListAdapter<Component<*>, ComposeRecyclerViewAdapter.Holder>(ITEM_CALLBACK) {

    private var lastItemViewTypeRequest: Component<*>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component =
            lastItemViewTypeRequest ?: currentList.first { it.getViewType() == viewType }
        val view = component.createView(parent)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position) as Component<View>)
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = getItem(position).key.hashCode().toLong()

    override fun getItemViewType(position: Int): Int {
        val component = getItem(position)
        lastItemViewTypeRequest = component
        return component.getViewType().hashCode()
    }

    private fun Component<*>.getViewType(): Any = viewType to children.map { it.getViewType() }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {

        private var boundComponent: Component<View>? = null

        fun bind(component: Component<View>) {
            boundComponent = component
            component.bindView(view)
        }

        fun unbind() {
            boundComponent?.unbindView(view)
        }
    }

    private companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Component<*>>() {
            override fun areItemsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem.key == newItem.key

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                true
        }
    }
}