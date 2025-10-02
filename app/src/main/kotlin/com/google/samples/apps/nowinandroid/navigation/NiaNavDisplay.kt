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

package com.google.samples.apps.nowinandroid.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.samples.apps.nowinandroid.core.navigation.NiaNavigator
import com.google.samples.apps.nowinandroid.core.navigation.NiaNavKey
import com.google.samples.apps.nowinandroid.feature.bookmarks.api.navigation.BookmarksRoute
import com.google.samples.apps.nowinandroid.feature.foryou.api.navigation.ForYouRoute
import com.google.samples.apps.nowinandroid.feature.interests.api.navigation.InterestsRoute
import kotlin.collections.forEach
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NiaNavDisplay(
    niaNavigator: NiaNavigator,
    entryProviderBuilders: Set<EntryProviderScope<NiaNavKey>.() -> Unit>,
) {
    val listDetailStrategy = rememberListDetailSceneStrategy<NiaNavKey>()
    // map of top level keys to their list of decorators
    val decoratorsStore = remember { mutableMapOf<NiaNavKey, List<NavEntryDecorator<NiaNavKey>>>() }

    /***
     * ONE WAY
     */
    val TOP_LEVEL_KEYS = listOf(ForYouRoute, BookmarksRoute, InterestsRoute())
    TOP_LEVEL_KEYS.forEach { niaKey ->
        key(niaKey) {
            val dec1 = rememberSavedStateNavEntryDecorator<NiaNavKey>()
            val dec2 = rememberViewModelStoreNavEntryDecorator<NiaNavKey>()
            decoratorsStore[niaKey] = listOf(
                dec1, dec2
            )
        }
    }


    /***
     * SECOND WAY
     */
    val currTopLevelKey = niaNavigator.currentActiveTopLevelKey
    key(currTopLevelKey) {
        if (!decoratorsStore.contains(currTopLevelKey)) {
            val dec1 = rememberSavedStateNavEntryDecorator<NiaNavKey>()
            val dec2 = rememberViewModelStoreNavEntryDecorator<NiaNavKey>()
            decoratorsStore[currTopLevelKey] = listOf(
                dec1, dec2
            )
        }
    }

    val entries = getCurrentEntries(
        niaNavigator.activeTopLeveLKeys,
        niaNavigator.backStackStore,
        entryProviderBuilders,
        decoratorsStore,
    )
    println("cfok display currentEntries: ${entries.map { it.contentKey }}")
    NavDisplay(
        entries = entries,
        sceneStrategy = listDetailStrategy,
        onBack = { niaNavigator.pop() },
    )
}

@Composable
fun getCurrentEntries(
    activeTopLeveLKeys: SnapshotStateList<NiaNavKey>,
    backStackStore: MutableMap<NiaNavKey, SnapshotStateList<NiaNavKey>>,
    entryProviderBuilders: Set<EntryProviderScope<NiaNavKey>.() -> Unit>,
    decoratorsStore: Map<NiaNavKey, List<NavEntryDecorator<NiaNavKey>>>,
): List<NavEntry<NiaNavKey>> =

    activeTopLeveLKeys.fold(emptyList()) { entries, topLevelKey ->
        val decorated = key(topLevelKey) {
            rememberDecoratedNavEntries(
                backStack = backStackStore[topLevelKey]!!,
                entryDecorators = decoratorsStore[topLevelKey]!!,
                entryProvider = entryProvider {
                    entryProviderBuilders.forEach { builder ->
                        builder()
                    }
                },
            )
        }

        println("cfok folding entries ${topLevelKey::class.simpleName}")
        entries + decorated
    }

//@Composable
//private fun generateRememberedDecoratedNavEntries(
//    backStack: SnapshotStateList<NiaNavKey>,
//    entryProviderBuilders: Set<EntryProviderScope<NiaNavKey>.() -> Unit>,
//    decorators: List<NavEntryDecorator<NiaNavKey>>
//): List<NavEntry<NiaNavKey>> {
//    require(backStack.isNotEmpty()) { "Cannot create decoratedNavEntries from empty backStack" }
//    require(backStack.first().isTopLevel) {
//        "decoratedNavEntries should only be created from a top level key"
//    }
//    return rememberDecoratedNavEntries(
//        backStack = backStack,
//        entryDecorators = decorators,
//        entryProvider = entryProvider {
//            entryProviderBuilders.forEach { builder ->
//                builder()
//            }
//        },
//    )
//}
