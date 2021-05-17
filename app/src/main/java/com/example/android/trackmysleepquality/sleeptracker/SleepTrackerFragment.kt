/*
 * Copyright 2018, The Android Open Source Project
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
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.databinding.FragmentSleepTrackerBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment with buttons to record start and end times for sleep, which are saved in
 * a database. Cumulative data is displayed in a simple scrollable TextView.
 * (Because we have not learned about RecyclerView yet.)
 */
class SleepTrackerFragment : Fragment() {

    /**
     * Called when the Fragment is ready to display content to the screen.
     *
     * This function uses DataBindingUtil to inflate R.layout.fragment_sleep_quality.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        val binding: FragmentSleepTrackerBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_sleep_tracker, container, false)

        binding.sleepList.layoutManager = GridLayoutManager(activity,3)
        //For linear displaying of ViewHolders
//        binding.sleepList.layoutManager = LinearLayoutManager(activity)

        val application = requireNotNull(this.activity).application

        val sleepDatabaseDao = SleepDatabase.getInstance(application).sleepDatabaseDao

        val viewModelFactory = SleepTrackerViewModelFactory(sleepDatabaseDao, application)

        val sleepTrackerViewModel = ViewModelProvider(this, viewModelFactory).get(SleepTrackerViewModel::class.java)
//        val sleepTrackerViewModel = SleepTrackerViewModel(sleepDatabaseDao,application) // why not like this?

        binding.lifecycleOwner = this

        binding.startButton.setOnClickListener { sleepTrackerViewModel.onStartTracking() }
        binding.stopButton.setOnClickListener { sleepTrackerViewModel.onStopTracking() }
        binding.clearButton.setOnClickListener { sleepTrackerViewModel.onClear() }
        /** Setting up LiveData nightsString observation relationship **/
        sleepTrackerViewModel.nightsString.observe(viewLifecycleOwner) { newNights ->
//            binding.textview.text = newNights
        }
        sleepTrackerViewModel.navigateToSleepQuality.observe(viewLifecycleOwner) { night ->
            night?.let {
                this.findNavController().navigate(
                        SleepTrackerFragmentDirections
                                .actionSleepTrackerFragmentToSleepQualityFragment(night.nightId))
                sleepTrackerViewModel.doneNavigating()
            }

        }

        sleepTrackerViewModel.startButtonVisible.observe(viewLifecycleOwner) { isEnabled ->
            binding.startButton.isEnabled = isEnabled
        }
        sleepTrackerViewModel.stopButtonVisible.observe(viewLifecycleOwner) { isEnabled ->
            binding.stopButton.isEnabled = isEnabled
        }
        sleepTrackerViewModel.clearButtonVisible.observe(viewLifecycleOwner) { isEnabled ->
            binding.clearButton.isEnabled = isEnabled
        }

        sleepTrackerViewModel.showSnackbarEvent.observe(viewLifecycleOwner) {
            if (it) {
                Snackbar.make(
                        activity!!.findViewById(android.R.id.content),
                        getString(R.string.cleared_message),
                        Snackbar.LENGTH_SHORT
                ).show()
                sleepTrackerViewModel.doneShowingSnackbar()
            }
        }

        val adapter = SleepNightAdapter()
        binding.sleepList.adapter = adapter

        sleepTrackerViewModel.nights.observe(viewLifecycleOwner) { newNights ->
            newNights?.let {
                adapter.submitList(it)
            }
        }

        return binding.root
    }
}
