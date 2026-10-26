/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package at.htl.android_frontend

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import at.htl.android_frontend.camera.ImageReferenceAdapter
import at.htl.android_frontend.camera.ImageReference
import at.htl.android_frontend.detail.CheckpointAdapter
import at.htl.android_frontend.network.Checkpoint
import at.htl.android_frontend.network.Path
import at.htl.android_frontend.overview.JunglePathAdapter

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView,
                     data: List<Path>?) {
    val adapter = recyclerView.adapter as JunglePathAdapter
    adapter.submitList(data)
}

@BindingAdapter("checkpointData")
fun bindCheckpointRecyclerView(recyclerView: RecyclerView,
                     data: List<Checkpoint>?) {
    val adapter = recyclerView.adapter as CheckpointAdapter
    adapter.submitList(data)
}
@BindingAdapter("photoData")
fun bindPhotoRecyclerView(recyclerView: RecyclerView,
                               data: List<ImageReference>?) {
    val adapter = recyclerView.adapter as ImageReferenceAdapter
    adapter.submitList(data.orEmpty())
}