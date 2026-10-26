package at.htl.android_frontend.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import at.htl.android_frontend.camera.ImageReference
import at.htl.android_frontend.camera.Photobook

class GalleryViewModel(
    photoList: Photobook,
    app: Application) : AndroidViewModel(app) {
    private val _propertyPhoto = MutableLiveData<List<ImageReference>>()
    val propertyPhoto: LiveData<List<ImageReference>>
        get() = _propertyPhoto
    init {
        _propertyPhoto.value = photoList.photos!!
    }

    }