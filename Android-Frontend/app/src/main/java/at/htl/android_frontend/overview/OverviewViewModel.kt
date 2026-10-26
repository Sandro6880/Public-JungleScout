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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.htl.android_frontend.network.JungleScoutApi
import at.htl.android_frontend.network.Path
import kotlinx.coroutines.launch

/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {

    // The internal MutableLiveData String that stores the most recent response
    private val _response = MutableLiveData<String>()
    private val _properties = MutableLiveData<List<Path>>()
    private val _navigateToSelectedPath = MutableLiveData<Path>()

    // The external immutable LiveData for the response String
    val response: LiveData<String>
        get() = _response
    val navigateToSelectedPath: LiveData<Path>
        get() = _navigateToSelectedPath
    val property: LiveData<List<Path>>
        get() = _properties

    init {
        getPaths()
    }

    /**
     * This function calls the Get-Request with retrofit for all Paths.
     */
    private fun getPaths() {
        viewModelScope.launch {
            try {
                _properties.value = JungleScoutApi.retrofitService.getPathList()
                _response.value =
                        "Success: Path properties retrieved"
            } catch (e: Exception) {
                _response.value = "Failure: ${e.message}"
            }
        }
    }

    /**
     * This function sets the value of the selected Path by the user.
     */
    fun displayPathDetails(displayPath: Path) {
        _navigateToSelectedPath.value = displayPath
    }

    /**
     * This function unsets the value of the previous selection when the App has redirected the user to the Path Details view.
     */
    fun displayPathDetailsComplete() {
        _navigateToSelectedPath.value = null
    }
}

