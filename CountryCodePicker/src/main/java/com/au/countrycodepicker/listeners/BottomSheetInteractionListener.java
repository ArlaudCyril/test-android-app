package com.au.countrycodepicker.listeners;

import android.view.View;

public interface BottomSheetInteractionListener {

    void initiateUi(View view);

    void setCustomStyle(View view);

    void setSearchEditText(View view);

    void setupRecyclerView(View view);
}
