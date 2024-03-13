package com.Lyber.ui.fragments

import android.content.ContentValues.TAG
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.Lyber.R
import com.Lyber.databinding.FragmentSearchAddressBinding
import com.Lyber.databinding.ItemLocationTextBinding
import com.Lyber.ui.adapters.BaseAdapter
import com.Lyber.ui.adapters.PlaceAutocomplete
import com.Lyber.ui.adapters.PlacesArrayAdapter
import com.Lyber.utils.CommonMethods.Companion.showToast
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchAddressFragment : BaseFragment<FragmentSearchAddressBinding>() {

    private lateinit var placeLocationUtil: PlacesArrayAdapter
    private lateinit var locationAdapter: LocationAdapter
//    private val addressText: String get() = binding.etSearch.text.trim().toString()
    override fun bind()= FragmentSearchAddressBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        placeLocationUtil = PlacesArrayAdapter(requireContext())
//        locationAdapter = LocationAdapter(::locationSelected)
        // Fetching API_KEY which we wrapped
//        val ai: ApplicationInfo = this.packageManager
//            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
//        val value = ai.metaData["api_key"]
//        val apiKey = value.toString()
//
//        // Initializing the Places API
//        // with the help of our API_KEY
//        if (!Places.isInitialized()) {
//            Places.initialize(applicationContext, apiKey)
//        }

        // Initialize Autocomplete Fragments
        // from the main activity layout file
        val autocompleteSupportFragment1 = childFragmentManager.findFragmentById(R.id.autocomplete_fragment1) as AutocompleteSupportFragment?

        // Information that we wish to fetch after typing
        // the location and clicking on one of the options
        autocompleteSupportFragment1!!.setPlaceFields(
            listOf(

                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL

            )
        )

        // Display the fetched information after clicking on one of the options
        autocompleteSupportFragment1.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(requireContext(),"Some error occurred", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {

                // Text view where we will
                // append the information that we fetch
                val textView = binding.tv1

                // Information about the place
                val name = place.name
                val address = place.address
                val phone = place.phoneNumber.toString()
                val latlng = place.latLng
                val latitude = latlng?.latitude
                val longitude = latlng?.longitude

                val isOpenStatus : String = if(place.isOpen == true){
                    "Open"
                } else {
                    "Closed"
                }

                val rating = place.rating
                val userRatings = place.userRatingsTotal

                textView.text = "Name: $name \nAddress: $address \nPhone Number: $phone \n" +
                        "Latitude, Longitude: $latitude , $longitude \nIs open: $isOpenStatus \n" +
                        "Rating: $rating \nUser ratings: $userRatings"
            }

//            override fun onError(status: Status) {
//                Toast.makeText(requireContext(),"Some error occurred", Toast.LENGTH_SHORT).show()
//            }
        })

//    }




//    binding.rvLocations.adapter = locationAdapter
//        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
//
//        binding.etSearch.addTextChangedListener(onTextChange)
//        binding.ivSearchClose.setOnClickListener {
//            binding.etSearch.setText("")
//        }
    }

//    private val onTextChange = object : TextWatcher {
//        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val list = placeLocationUtil.getPredictions(addressText)
//                    requireActivity().runOnUiThread {
//                        binding.rvLocations.visibility = View.VISIBLE
//                        Log.d(TAG, "getPredictions: $list")
////                        locationAdapter.clear()
//                        locationAdapter.setList(list)
//                    }
//                } catch (e: Exception) {
//
//                }
//            }
//        }
//
//        override fun afterTextChanged(p0: Editable?) {}
//    }
//    private fun locationSelected(result: PlaceAutocomplete?) {
//        binding.etSearch.removeTextChangedListener(onTextChange)
//        result?.let {
//            binding.etSearch.setText(result.toString())
//            binding.etSearch.clearFocus()
//            binding.rvLocations.visibility = View.GONE
////            binding.rlProgressView.visibility=View.VISIBLE
//            Places.createClient(requireContext()).fetchPlace(
//                FetchPlaceRequest.newInstance(
//                    (result?.placeId ?: "").toString(),
//                    listOf(Place.Field.LAT_LNG)
//                )
//            ).addOnSuccessListener {
////                binding.rlProgressView.visibility=View.GONE
//            }.addOnFailureListener {
//                "Couldn't fetch location".showToast(requireContext())
////                binding.rlProgressView.visibility=View.GONE
//            }
//        }
//        binding.etSearch.addTextChangedListener(onTextChange)
//    }

    class LocationAdapter(private val handle: (PlaceAutocomplete) -> Unit) :
        BaseAdapter<PlaceAutocomplete>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return LocationViewHolder(
                ItemLocationTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            itemList[position]?.let {
                (holder as LocationViewHolder).binding.apply {
                    tvLocationAddress.text = it.toString()
                }
            }
        }

        inner class LocationViewHolder(val binding: ItemLocationTextBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    itemList[absoluteAdapterPosition]?.let {
                        handle.invoke(it)
                    }
                }
            }
        }
    }
}