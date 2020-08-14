package com.gifttome.gifttome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessAdapter extends FragmentPagerAdapter {
    public TabsAccessAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                MyPostsFragment myPostsFragment = new MyPostsFragment();
                return myPostsFragment;
            case 1:
                AvailablePostsFragment availablePostsFragment = new AvailablePostsFragment();
                return availablePostsFragment;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){

            case 0:
                return "My Posts";
            case 1:
                return "Available Posts";

            default:
                return null;

        }
    }
}
