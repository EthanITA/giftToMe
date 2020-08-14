package com.gifttome.gifttome;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private TabsAccessAdapter myTabsAccessorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisFragment = inflater.inflate(R.layout.fragment_main, container, false);
        //mToolbar = thisFragment.findViewById(R.id.main_page_toolbar);
        //((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        //((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("GiftToMe main fragment");
        mViewPager = thisFragment.findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessAdapter(getActivity().getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(myTabsAccessorAdapter);

        mTabLayout = thisFragment.findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        ChatsFragment newChatFragment = new ChatsFragment();
        /*getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.frag_container, newChatFragment)
                .addToBackStack(null)
                .commit();

         */

        return thisFragment;
    }

}
