package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

        val navigateToSleepQuality: LiveData<SleepNight>
                get() = _navigateToSleepQuality

        private val tonight = MutableLiveData<SleepNight?>()
        private val nights = database.getAllNights()

        val nightsString = Transformations.map(nights) { nights ->
                formatNights(nights, application.resources)
        }

        val startButtonVisible = Transformations.map(tonight) {
                it == null
        }
        val stopButtonVisible = Transformations.map(tonight) {
                it != null
        }
        val clearButtonVisible = Transformations.map(nights) {
                it?.isNotEmpty()
        }

        private var _showSnackbarEvent = MutableLiveData<Boolean>()

        val showSnackBarEvent: LiveData<Boolean>
                get() = _showSnackbarEvent

        init {
                initializeTonight()
        }

        fun doneShowingSnackbar() {
                _showSnackbarEvent.value = false
        }

        fun doneNavigating() {
                _navigateToSleepQuality.value = null
        }

        private fun initializeTonight() {
                viewModelScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                var night = database.getTonight()
                if (night?.endTimeMilli != night?.startTimeMilli) {
                        night = null
                }
                return night
        }

        fun onStartTracking(){
                viewModelScope.launch{
                        val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()
                }

        }

        private suspend fun insert(night: SleepNight){
                database.insert(night)
        }

        fun onStopTracking(){
                viewModelScope.launch {
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)

                        _navigateToSleepQuality.value = oldNight
                }
        }

        private suspend fun update(night: SleepNight){
                database.update(night)
        }

        fun onClear() {
                viewModelScope.launch {
                        clear()
                        tonight.value = null

                        _showSnackbarEvent.value = true
                }
        }

        private suspend fun clear() {
                database.clear()
        }

}

