/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.nowinandroid.core.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.collections.mutableListOf

class NiaBackStack(
    private val startKey: NiaNavKey,
) {
    var backStackMap: LinkedHashMap<NiaNavKey, MutableList<NiaNavKey>> =
        linkedMapOf(
            startKey to mutableListOf(startKey)
        )


    val backStack: SnapshotStateList<NiaNavKey> = mutableStateListOf(startKey)

    var currentTopLevelKey: NiaNavKey by mutableStateOf(backStackMap.keys.first())
        private set

    val currentKey: NiaNavKey
        get() = backStackMap[currentTopLevelKey]!!.last()

    fun navigate(key: NiaNavKey) {
        when {
            // top level singleTop -> clear substack
            key == currentTopLevelKey -> backStackMap[key] = mutableListOf(key)
            // top level non-singleTop
            key.isTopLevel -> {
                // if navigating back to start destination, pop all other top destinations and
                // store start destination substack
                if (key == startKey) {
                    val tempStack = mapOf(startKey to backStackMap[startKey]!!)
                    backStackMap.clear()
                    backStackMap.putAll(tempStack)
                // else either restore an existing substack or initiate new one
                } else {
                    backStackMap[key] = backStackMap.remove(key) ?: mutableListOf(key)
                }

            }
            // not top level - add to current substack
            else -> backStackMap.values.last().add(key)
        }
        updateBackStack()
    }

    fun removeLast() {
        if (currentKey == currentTopLevelKey) {
            backStackMap.remove(currentTopLevelKey)
        } else {
            backStackMap[currentTopLevelKey]!!.removeLastOrNull()
        }
        updateBackStack()
    }

    fun updateBackStack() {
        backStack.apply {
            clear()
            backStackMap.forEach {
                backStack.addAll(it.value)
            }
        }
        currentTopLevelKey = backStackMap.keys.last()
    }

    fun restore(map: LinkedHashMap<NiaNavKey, MutableList<NiaNavKey>>?) {
        map ?: return
        backStackMap.clear()
        backStackMap.putAll(map)
        updateBackStack()
    }
}

interface NiaNavKey {
    val isTopLevel: Boolean
}