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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import at.htl.android_frontend.network.Checkpoint

/**
 * The [ViewModel] that is associated with the [DetailFragment].
 */
class CheckpointDetailViewModel(
        givenCheckpoint: Checkpoint,
        app: Application) : AndroidViewModel(app) {
    private val _propertyCheckpoint = MutableLiveData<Checkpoint>()
    val propertyCheckpoint: LiveData<Checkpoint>
        get() = _propertyCheckpoint

    init {
        _propertyCheckpoint.value = givenCheckpoint
    }
}
