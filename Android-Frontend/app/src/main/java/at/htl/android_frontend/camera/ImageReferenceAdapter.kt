package at.htl.android_frontend.camera

import android.graphics.BitmapFactory

import android.graphics.Bitmap

import android.content.Context

import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater

import android.view.ViewGroup

import at.htl.android_frontend.network.DataBaseHandler

import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import at.htl.android_frontend.databinding.PhotoDbItemBinding
import kotlinx.android.synthetic.main.photo_db_item.view.*

/**
 * The ImageReferenceAdapter class provides the necessary functions for a working Recycler View that
 * handles a list of Images.
 */
class ImageReferenceAdapter(private val context: Context?,
                            private val singleRowArrayList: ArrayList<ImageReference>,
                            private val db: SQLiteDatabase?,
                            private val myDatabase: DataBaseHandler?) : ListAdapter<ImageReference,
        ImageReferenceAdapter.LocalDataBaseViewHolder>(DiffCallback) {
    class LocalDataBaseViewHolder(private var binding: PhotoDbItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cp: ImageReference) {
            binding.property = cp
            binding.executePendingBindings()

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            LocalDataBaseViewHolder {
        return LocalDataBaseViewHolder(
            PhotoDbItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    /*fun onBindViewHolder(myViewHolder: MyViewHolder, i: Int) {
        myViewHolder.newsImage.setImageBitmap(getBitmapFromEncodedString(singleRowArrayList.get(i).image))
        myViewHolder.delete.setOnClickListener(object : OnClickListener() {
            fun onClick(v: View?) {
                deletedata(i, singleRowArrayList)
            }
        })
    }*/
    override fun onBindViewHolder(holder: LocalDataBaseViewHolder, position: Int) {
        val cp = getItem(position)
        holder.itemView.imageImage.setImageBitmap(cp.image?.let { getBitmapFromEncodedString(it) })
        //holder.itemView.delete.setOnClickListener { deletedata(position,singleRowArrayList) }
        holder.bind(cp)
    }
    override fun getItemCount(): Int {
        return singleRowArrayList.size
    }

    /*fun deletedata(position: Int, singleRowArrayList: ArrayList<LocalResponse>?) {
        if (context != null) {
            AlertDialog.Builder(context)
                .setTitle("Delete result")
                .setMessage("Are you sure you want delete this result?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    /* This is where deletions should be handled */
                    if (singleRowArrayList != null) {
                        myDatabase!!.deleteEntry(singleRowArrayList.get(position).uid.toLong())
                    }
                    if (singleRowArrayList != null) {
                        singleRowArrayList.removeAt(position)
                    }
                    notifyItemRemoved(position)
                    notifyDataSetChanged()
                    myDatabase!!.close()
                    //((MainActivity) context).loadFragment(new LocalFragment(), true);
                })
                .setNegativeButton("No", null)
                .show()
        }
    }*/

    private fun getBitmapFromEncodedString(encodedString: String): Bitmap? {
        val arr: ByteArray = Base64.decode(encodedString, Base64.URL_SAFE)
        return BitmapFactory.decodeByteArray(arr, 0, arr.size)
    }
    companion object DiffCallback : DiffUtil.ItemCallback<ImageReference>() {
        override fun areItemsTheSame(oldItem: ImageReference, newItem: ImageReference): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ImageReference, newItem: ImageReference): Boolean {
            return oldItem.uid == newItem.uid
        }
    }

}