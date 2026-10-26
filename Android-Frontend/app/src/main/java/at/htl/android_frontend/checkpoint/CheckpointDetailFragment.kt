/*
 *  Copyright 2019, The Android Open Source Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.htl.android_frontend.checkpoint

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.htl.android_frontend.camera.ImageReference
import at.htl.android_frontend.databinding.FragmentCheckpointDetailBinding
import at.htl.android_frontend.network.DataBaseHandler

import androidx.recyclerview.widget.RecyclerView
import at.htl.android_frontend.camera.Photobook
import at.htl.android_frontend.network.Checkpoint


/**
 * This [Fragment] will show the detailed information about a selected Path.
 */
class CheckpointDetailFragment : Fragment() {
    //Bitmap photo;
    var recyclerView: RecyclerView? = null
    private var singleRowArrayList: ArrayList<ImageReference>? = null
    var databaseHandler: DataBaseHandler? = null
    private var db: SQLiteDatabase? = null
    var cursor: Cursor? = null
    var cp: Checkpoint? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val application = requireNotNull(activity).application
        val binding = FragmentCheckpointDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this
        cp = CheckpointDetailFragmentArgs.fromBundle(requireArguments()).selectedCheckpoint
        val viewModelFactory = CheckpointDetailViewModelFactory(cp!!,application)
        val vM = ViewModelProvider(
                this, viewModelFactory).get(CheckpointDetailViewModel::class.java)
        binding.viewModel = vM
        databaseHandler = getContext()?.let { DataBaseHandler(it) }
        db = databaseHandler?.getWritableDatabase();
        //db?.let { databaseHandler?.onUpgrade(it,1,2) }
        /*recyclerView = binding.photoGrid

        binding.photoGrid.adapter = recyclerView?.adapter*/

        binding.buttonView.setOnClickListener(View.OnClickListener {
            setData()
            if(singleRowArrayList!!.size != 0){
                this.findNavController().navigate(
                    CheckpointDetailFragmentDirections.actionCheckpointDetailFragmentToGalleryFragment(Photobook(singleRowArrayList)))
            }else{
                Toast.makeText(context,"No Pictures for this Checkpoint.", Toast.LENGTH_SHORT).show()
            }

        })
        return binding.root
    }
    /**
     * Calls the local database to receive the pictures which were already taken and
     * uses them for further development.
     */
    private fun setData() {
        db = databaseHandler?.getWritableDatabase()
        singleRowArrayList = ArrayList()
        val columns = arrayOf(DataBaseHandler.KEY_ID, DataBaseHandler.KEY_IMG_URL,DataBaseHandler.F_KEY_ID)
        cursor = db!!.query(DataBaseHandler.TABLE_NAME, columns,
            null, null, null, null, null)
        while (cursor?.moveToNext() == true) {
            val index1: Int? = cursor?.getColumnIndex(DataBaseHandler.KEY_ID)
            val index2: Int? = cursor?.getColumnIndex(DataBaseHandler.KEY_IMG_URL)
            val index3: Int? = cursor?.getColumnIndex(DataBaseHandler.F_KEY_ID)
            val uid = index1?.let { cursor?.getInt(it) }
            val image = index2?.let { cursor?.getString(it) }
            val cp_id = index3?.let { cursor?.getLong(it) }
            var singleRow: ImageReference? = null
            if(cp_id == cp?.id){
                if(cp_id != null && uid != null){
                    singleRow = ImageReference(image,uid, cp_id)
                }
            }
            if (singleRow != null) {
                singleRowArrayList!!.add(singleRow)
            }
        }
    }


}