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

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.createView
import com.ivianuu.compose.sample.common.Navigator
import com.ivianuu.compose.setContent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

private fun ViewComposition.App() {
    View<FrameLayout>("CraneWrapper ${System.currentTimeMillis()}") {
        createView()

        bindView {
            layoutParams =
                (layoutParams ?: ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)).apply {
                    width = MATCH_PARENT
                    height = MATCH_PARENT
                }
        }

        Navigator {
            Home2()
        }
    }
}