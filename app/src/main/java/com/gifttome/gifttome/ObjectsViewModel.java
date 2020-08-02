package com.gifttome.gifttome;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;


//potrebbe non servire
public class ObjectsViewModel extends ViewModel {
        private ArrayList<AvailableObjectsData> availableObjectsData;

        public ArrayList<AvailableObjectsData> getUsers() {
            if (availableObjectsData == null) {
                availableObjectsData = new ArrayList<>();
                loadObjects();
            }
            return availableObjectsData;
        }

        private void loadObjects() {
            // Do an asynchronous operation to fetch users.
        }
    }

