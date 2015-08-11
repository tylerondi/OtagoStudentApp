package edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.weber.tondricek.cs4750.otagostudentapp.otagostudentapp.tabs.SlidingTabLayout;

public class CategoryQuestionHolderFragment extends Fragment {
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private MyPagerAdapter pagerAdapter;

    private CategoryFragment categoryFragment;
    private QuestionFragment questionFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_question_holder, container, false);
        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mTabs = (SlidingTabLayout) rootView.findViewById(R.id.tabs);

        pagerAdapter = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mTabs.setViewPager(mPager);

        return rootView;
    }

    public void setUp() {
        categoryFragment = new CategoryFragment();
        questionFragment = new QuestionFragment();
    }

    public void switchData() {
        categoryFragment.setUp();
        questionFragment.setUp();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Adapter ----------------------------------------------------
    class MyPagerAdapter extends FragmentStatePagerAdapter {

        String tabs[];

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return categoryFragment;
            } else {
                return questionFragment;
            }

        }

        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
