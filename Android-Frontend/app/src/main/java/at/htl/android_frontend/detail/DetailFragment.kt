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

package at.htl.android_frontend.detail

import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.htl.android_frontend.camera.ImageReference
import at.htl.android_frontend.camera.Photobook
import at.htl.android_frontend.databinding.FragmentDetailBindingImpl
import at.htl.android_frontend.network.Checkpoint
import at.htl.android_frontend.network.DataBaseHandler
import at.htl.android_frontend.network.JungleScoutApi
import java.lang.Exception

/**
 * This [Fragment] will show the detailed information about a selected Path.
 */
class DetailFragment : Fragment() {
    private var singleRowArrayList: ArrayList<ImageReference>? = null
    var databaseHandler: DataBaseHandler? = null
    private var db: SQLiteDatabase? = null
    var recyclerView: RecyclerView? = null
    var cursor: Cursor? = null
    var cp: Checkpoint? = null
    var email: String? = null

    private val backButtonCallback = object: OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            AlertDialog.Builder(requireContext())
                .setTitle("Quit the route?")
                .setMessage("Are you sure you want to quit this route? All progress will be lost.")
                .setPositiveButton("Yes", DialogInterface.OnClickListener{dialogInterface, i ->
                    requireActivity().supportFragmentManager.popBackStack()
                })
                .setNegativeButton("Cancel",null)
                .show()
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val application = requireNotNull(activity).application
        val binding = FragmentDetailBindingImpl.inflate(inflater)
        binding.lifecycleOwner = this
        databaseHandler = getContext()?.let { DataBaseHandler(it) }
        db = databaseHandler?.getWritableDatabase();

        val path = DetailFragmentArgs.fromBundle(requireArguments()).selectedProperty
        val viewModelFactory = DetailViewModelFactory(path, application)
        val vM = ViewModelProvider(
                this, viewModelFactory).get(DetailViewModel::class.java)
        binding.viewModel = vM
        binding.checkpointGrid.adapter = CheckpointAdapter(
            CheckpointAdapter.OnClickListener
        { vM.displayCheckpointDetails(it)})

        vM.selectedCheckpoint.observe(viewLifecycleOwner, Observer {
            if ( null != it) {
                this.findNavController().navigate(
                    DetailFragmentDirections.actionShowCheckpointDetail(it))
                vM.displayCheckpointDetailsComplete()
            }
        })
        binding.button.setOnClickListener(View.OnClickListener {
            if(vM.selectedProperty.value != null){
                this.findNavController().navigate(
                        DetailFragmentDirections.actionDetailFragmentToMapsFragment(vM.selectedProperty.value!!))
            }
        })
        requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner,backButtonCallback)
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Following Path: " + path.name
        binding.printButton.setOnClickListener ( View.OnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("End the tour?")
                .setMessage("Are you sure you want to end the tour now and print your photobook?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                    /* This is where the photobook should be handled */
                    val input: EditText = EditText(requireContext())
                    AlertDialog.Builder(requireContext())
                        .setTitle("Email")
                        .setMessage("Please write your email here for us to send you your Photobook.")
                        .setView(input)
                        .setPositiveButton("Ok", DialogInterface.OnClickListener{dialogInterface, i ->
                            email = input.text.toString()
                            setData()
                            Thread {
                                try {
                                    JungleScoutApi.retrofitService.createPhotobook(Photobook(singleRowArrayList,email)).execute()
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }.start()
                            this.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToOverviewPathFragment())
                            Toast.makeText(context,"Thank you for participating!", Toast.LENGTH_SHORT).show()
                        })
                        .setNegativeButton("Cancel",null)
                        .show()
                })
                .setNegativeButton("No", null)
                .show()

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
            if(cp_id != null && uid != null){
                singleRow = ImageReference(image,uid, cp_id)
            }
            if (singleRow != null) {
                singleRowArrayList!!.add(singleRow)
            }
        }
    }

}