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

package at.htl.android_frontend.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.htl.android_frontend.databinding.GridViewItemBinding
import at.htl.android_frontend.network.Path

/**
 * The JunglePathAdapter class provides the necessary functions for a working Recycler View that
 * handles a list of Paths.
 */
class JunglePathAdapter(private val onClickListener: OnClickListener) : ListAdapter<Path,
        JunglePathAdapter.PathViewHolder>(DiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            PathViewHolder {
        return PathViewHolder(GridViewItemBinding.inflate(
                LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
        val path = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(path)
        }
        holder.bind(path)
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Path>() {
        override fun areItemsTheSame(oldItem: Path, newItem: Path): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Path, newItem: Path): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class PathViewHolder(private var binding:
                                 GridViewItemBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(path: Path) {
            if(path.checkpoints.size != 0){
                binding.property = path
                binding.executePendingBindings()
            }
        }
    }
    class OnClickListener(val clickListener: (path:Path) -> Unit) {
        fun onClick(path:Path) = clickListener(path)
    }
}