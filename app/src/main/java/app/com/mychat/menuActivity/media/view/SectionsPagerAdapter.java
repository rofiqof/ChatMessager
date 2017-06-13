package app.com.mychat.menuActivity.media.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.com.mychat.menuActivity.media.tab.document.DocumentFragment;
import app.com.mychat.menuActivity.media.tab.media.MediaFragment;

/**
 * Created by rofiqoff on 5/22/17.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    String []tittle = new String[]{
            "Media", "Document"
    };

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0 :
                fragment = new MediaFragment();
                break;
            case 1 :
                fragment = new DocumentFragment();
                break;
            default:
                return fragment;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return tittle.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tittle[position];
    }
}
