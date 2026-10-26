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

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.htl.android_frontend.R
import at.htl.android_frontend.databinding.FragmentPathOverviewBinding
import at.htl.android_frontend.network.DataBaseHandler
import at.htl.android_frontend.network.DataBaseHandlerSaveMapInstance
import at.htl.android_frontend.overview.JunglePathAdapter

class OverviewFragment : Fragment() {

    /**
     * Lazily initialize [OverviewViewModel].
     */
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }
    var databaseHandler: DataBaseHandler? = null
    var instanceSaver: DataBaseHandlerSaveMapInstance? = null
    private var db: SQLiteDatabase? = null
    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the OverviewFragment
     * to enable Data Binding to observe LiveData, and sets up the RecyclerView with an adapter.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentPathOverviewBinding.inflate(inflater)
        //val binding = GridViewItemBinding.inflate(inflater)
        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel
        binding.pathGrid.adapter = JunglePathAdapter(JunglePathAdapter.OnClickListener
        {   viewModel.displayPathDetails(it) })
        viewModel.navigateToSelectedPath.observe(viewLifecycleOwner, Observer {
            if ( null != it ) {
                this.findNavController().navigate(
                        OverviewFragmentDirections.actionShowDetail(it))
                viewModel.displayPathDetailsComplete()
            }
        })
        //setHasOptionsMenu(true)
        binding.lifecycleOwner = this
        //resets the Database which stores photo's and the map's progress
        if(savedInstanceState == null){
            databaseHandler = getContext()?.let { DataBaseHandler(it) }
            db = databaseHandler?.getWritableDatabase();
            databaseHandler?.onUpgrade(db!!,1,1)
            instanceSaver = getContext()?.let { DataBaseHandlerSaveMapInstance(it) }
            db = instanceSaver?.getWritableDatabase();
            instanceSaver?.onUpgrade(db!!,1,1)

        }
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "JungleScout"
        return binding.root
    }

    /**
     * Inflates the overflow menu that contains filtering options.
     */
    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }*/
}
