package com.revel.humraahi.classes;

import android.app.Application;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class GetStates extends Application {
    public static String[] arrayStates = {"Andaman and Nicobar Islands",
            "Andhra Pradesh", "Arunachal Pradesh",
            "Assam", "Bihar", "Chandigarh",
            "Chhattisgarh", "Dadra and Nagar Haveli", "Daman and Diu",
            "Delhi", "Goa", "Gujarat", "Haryana",
            "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka",
            "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra",
            "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry",
            "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
            "Uttar Pradesh", "Uttarakhand", "West Bengal"};
    public static ArrayList<String> arrayListStates = new ArrayList<>();

    public static ArrayAdapter getAdapter(Context context) {
        return new ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayStates);
    }

    public static ArrayList getList() {
        for (int i = 0; i < arrayStates.length; i++) {
            arrayListStates.add(arrayStates[i]);
        }
        return arrayListStates;
    }
}
