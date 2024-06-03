package com.Lyber.ui.adapters

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PlacesArrayAdapter(context: Context) {

    private val placesClient: PlacesClient


    init {
        placesClient = Places.createClient(context)
    }

    suspend fun getPredictions(constraint: CharSequence): ArrayList<PlaceAutocomplete> {
        val resultList: ArrayList<PlaceAutocomplete> = ArrayList()

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()


        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                // .setLocationBias(bounds)
//                .setLocationBias(mBounds) //.setCountry("au")
                //   .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(constraint.toString())
                .build()
        val autocompletePredictions: Task<FindAutocompletePredictionsResponse> =

            placesClient.findAutocompletePredictions(request)

        // This method should have been called off the main UI thread. Block and wait for at most
        // 60s for a result from the API.

        try {
            Tasks.await(autocompletePredictions, 60, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
        return if (autocompletePredictions.isSuccessful) {
            val findAutocompletePredictionsResponse: FindAutocompletePredictionsResponse =
                autocompletePredictions.result
            if (findAutocompletePredictionsResponse != null)
                for (prediction in findAutocompletePredictionsResponse.autocompletePredictions) {
                    resultList.add(
                        PlaceAutocomplete(
                            prediction.placeId,
                            prediction.getFullText(null).toString(),
                        )
                    )
                }
            resultList
        } else resultList
    }


    companion object {
        private const val TAG = "PlaceArrayAdapter"
    }
}

class PlaceAutocomplete internal constructor(
    var placeId: CharSequence,
    var description: CharSequence,
) {
    override fun toString(): String {
        return description.toString()
    }

}