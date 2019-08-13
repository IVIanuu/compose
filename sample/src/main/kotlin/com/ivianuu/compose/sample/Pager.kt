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

package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.memo
import androidx.compose.state
import androidx.ui.graphics.Color
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.FadeChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.TabItem
import com.ivianuu.compose.sample.common.TabLayout
import com.ivianuu.compose.sample.common.ViewPager
import kotlinx.android.synthetic.main.page.view.*

val AllColors = arrayOf(
    Color.Black,
    Color.DarkGray,
    Color.Gray,
    Color.LightGray,
    Color.White,
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Cyan,
    Color.Magenta,
    Color.Transparent,
    Color.Aqua,
    Color.Fuchsia,
    Color.Lime,
    Color.Maroon,
    Color.Navy,
    Color.Olive,
    Color.Purple,
    Color.Silver,
    Color.Teal
)

fun ViewComposition.Pager() = Route {
    val transition = +memo { FadeChangeHandler() }
    ChangeHandlers(handler = transition) {
        var selectedPage by +state { 0 }

        Scaffold(
            appBar = {
                TabLayout(
                    selectedIndex = selectedPage,
                    onTabChanged = { selectedPage = it },
                    children = {
                        (1..5).forEach { i ->
                            TabItem("Tab $i")
                        }
                    }
                )
            },
            content = {
                ViewPager(
                    selectedPage = selectedPage,
                    onPageChanged = { selectedPage = it },
                    children = {
                        (1..5).forEach { i ->
                            group(i) {
                                val color = +memo { AllColors.toList().shuffled()[i] }
                                Page(i, color)
                            }
                        }
                    }
                )
            }
        )
    }
}

private fun ViewComposition.Page(
    index: Int,
    color: Color
) {
    View<View> {
        layoutRes(R.layout.page)
        bindView {
            page_bg.setBackgroundColor(color.toArgb())
            page_text.text = "#$index"
        }
    }
}
