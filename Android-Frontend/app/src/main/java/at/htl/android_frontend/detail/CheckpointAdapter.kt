package at.htl.android_frontend.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.htl.android_frontend.databinding.CheckpointViewItemBinding
import at.htl.android_frontend.network.Checkpoint

/**
 * The CheckpointAdapter class provides the necessary functions for a working Recycler View that
 * handles a list of Checkpoints.
 */
class CheckpointAdapter(private val onClickListener: OnClickListener) : ListAdapter<Checkpoint,
        CheckpointAdapter.CheckpointViewHolder>(DiffCallback){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            CheckpointViewHolder {
        return CheckpointViewHolder(
            CheckpointViewItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: CheckpointViewHolder, position: Int) {
        val cp = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(cp)
        }
        holder.bind(cp)
    }
    companion object DiffCallback : DiffUtil.ItemCallback<Checkpoint>() {
        override fun areItemsTheSame(oldItem: Checkpoint, newItem: Checkpoint): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Checkpoint, newItem: Checkpoint): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class CheckpointViewHolder(private var binding:
                         CheckpointViewItemBinding
    ):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cp: Checkpoint) {
            binding.property = cp
            binding.executePendingBindings()

        }
    }
    class OnClickListener(val clickListener: (cp: Checkpoint) -> Unit) {
        fun onClick(cp: Checkpoint) = clickListener(cp)
    }
}