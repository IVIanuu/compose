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

package com.ivianuu.compose.sample.common

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.component
import com.ivianuu.compose.getViewManager
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.TabLayout(
    selectedIndex: Int,
    noinline onTabChanged: (Int) -> Unit,
    noinline children: ViewComposition.() -> Unit
) {
    TabLayout(sourceLocation(), selectedIndex, onTabChanged, children)
}

fun ViewComposition.TabLayout(
    key: Any,
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    children: ViewComposition.() -> Unit
) {
    View<TabLayout>(key = key) {
        layoutRes(R.layout.tab_layout)

        manageChildren = true

        bindView {
            component!!.children
                .mapIndexed { i, child ->
                    var tab = getTabAt(i)
                    if (tab == null) {
                        tab = newTab()
                        tab.customView = FrameLayout(context).apply {
                            layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                        }
                        addTab(tab, i)
                    }

                    child to tab
                }
                .forEach { (child, tab) ->
                    (tab.customView as ViewGroup).getViewManager()
                        .update(listOf(child))
                }

            while (tabCount > component!!.children.size) {
                removeTabAt(tabCount - 1)
            }

            selectTab(getTabAt(selectedTab))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    onTabChanged(tab.position)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }
            })
        }

        unbindView {
            (0 until tabCount)
                .forEach {
                    (getTabAt(it)!!.customView as FrameLayout)
                        .getViewManager().clear()
                }
            removeAllTabs()
        }

        children()
    }
}

fun ViewComposition.TabItem(text: String) {
    View<TextView>(text) {
        layoutRes(R.layout.tab_item)
        bindView { this.text = text }
    }
}