package com.ivianuu.compose.sample.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.Component
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.RecyclerView(
    noinline children: ViewComposition.() -> Unit
) {
    RecyclerView(sourceLocation(), children)
}

fun ViewComposition.RecyclerView(
    key: Any,
    children: ViewComposition.() -> Unit
) {
    emit(
        key = key,
        ctor = { RecyclerViewComponent() },
        update = { children() }
    )
}

class RecyclerViewComponent : Component<RecyclerView>() {

    override fun createView(container: ViewGroup): RecyclerView {
        return RecyclerView(container.context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ComposeRecyclerViewAdapter()
        }
    }

    override fun bindView(view: RecyclerView) {
        super.bindView(view)
        (view.adapter as ComposeRecyclerViewAdapter).submitList(children.toList())
    }

}

private class ComposeRecyclerViewAdapter :
    ListAdapter<Component<*>, ComposeRecyclerViewAdapter.Holder>(ITEM_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component = currentList[viewType]
        val view = component.performCreateView(parent)
        return Holder(component as Component<View>, view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind()
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = getItem(position).key.hashCode().toLong()

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class Holder(
        val component: Component<View>,
        val view: View
    ) : RecyclerView.ViewHolder(view) {
        init {
            setIsRecyclable(false)
        }

        fun bind() {
            component.bindView(view)
        }

        fun unbind() {
            component.unbindView(view)
        }
    }

    private companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Component<*>>() {
            override fun areItemsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem.key == newItem.key

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem == newItem
        }
    }
}