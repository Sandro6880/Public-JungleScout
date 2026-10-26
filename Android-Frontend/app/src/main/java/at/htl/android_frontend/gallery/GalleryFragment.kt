package at.htl.android_frontend.gallery

import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.htl.android_frontend.camera.ImageReferenceAdapter
import at.htl.android_frontend.camera.ImageReference
import at.htl.android_frontend.camera.Photobook
import at.htl.android_frontend.databinding.GalleryFragmentBinding
import at.htl.android_frontend.network.DataBaseHandler

class GalleryFragment : Fragment() {
    private lateinit var viewModel: GalleryViewModel
    var photoList: Photobook? = null
    var databaseHandler: DataBaseHandler? = null
    private var db: SQLiteDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(activity).application
        val binding = GalleryFragmentBinding.inflate(inflater)
        photoList = GalleryFragmentArgs.fromBundle(requireArguments()).photoProperty
        val viewModelFactory = GalleryViewModelFactory(photoList!!,application)
        binding.lifecycleOwner = this
        databaseHandler = getContext()?.let { DataBaseHandler(it) }
        db = databaseHandler?.getWritableDatabase();
        binding.pathGrid.adapter = photoList!!.photos?.let {
            ImageReferenceAdapter(this.context,
                it as ArrayList<ImageReference>,db,databaseHandler)
        }
        viewModel = ViewModelProvider(
            this, viewModelFactory).get(GalleryViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

}