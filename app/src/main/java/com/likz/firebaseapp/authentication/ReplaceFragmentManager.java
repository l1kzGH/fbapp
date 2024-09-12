package com.likz.firebaseapp.authentication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jetbrains.annotations.NotNull;

public class ReplaceFragmentManager {

    void replace(@NotNull Fragment currentFragment, @NotNull Fragment updateFragment, int fragmentRId) {
        FragmentManager fragmentManager = currentFragment.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(fragmentRId, updateFragment, null);
        fragmentTransaction.commit();
        // adding to backstack to back at fragment if pressed back button or swiped screen
        fragmentTransaction.addToBackStack(currentFragment.toString());
    }

}
