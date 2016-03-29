
package com.yuzhi.fine.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.model.PointRecord;
import com.jxtii.wildebeest.model.PositionRecord;
import com.jxtii.wildebeest.util.CommUtil;
import com.yuzhi.fine.R;
import com.yuzhi.fine.fragment.BufferKnifeFragment;
import com.yuzhi.fine.fragment.MainPagerFragment;
import com.yuzhi.fine.fragment.MemberFragment;
import com.yuzhi.fine.fragment.SensorFragment;
import com.yuzhi.fine.fragment.WildebeestFragment;
import com.yuzhi.fine.ui.UIHelper;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends BaseFragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FRAGMENT_TAGS = "fragmentTags";
    private static final String CURR_INDEX = "currIndex";
    private static int currIndex = 0;

    private RadioGroup group;

    private ArrayList<String> fragmentTags;
    private FragmentManager fragmentManager;
    static final int GPS_OPEN_STATUS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            initData();
            initView();
        } else {
            initFromSavedInstantsState(savedInstanceState);
        }
        if (CommUtil.isOpenGPS(this)) {
            showToast("已开启GPS！");
            startLocService();
        } else {
            showToast("请开启GPS！");
            Intent intent = new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, GPS_OPEN_STATUS);
        }
    }

    void startLocService() {
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        long triggerAtTime = System.currentTimeMillis() + 5 * 1000;
        long interval = 15 * 60 * 1000;
        Intent intent = new Intent();
        intent.setAction(CommUtil.START_INTENT);
        intent.setPackage("com.yuzhi.fine");
        intent.putExtra("interval", 2000);
        PendingIntent pt = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, pt);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURR_INDEX, currIndex);
        outState.putStringArrayList(FRAGMENT_TAGS, fragmentTags);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initFromSavedInstantsState(savedInstanceState);
    }

    private void initFromSavedInstantsState(Bundle savedInstanceState) {
        currIndex = savedInstanceState.getInt(CURR_INDEX);
        fragmentTags = savedInstanceState.getStringArrayList(FRAGMENT_TAGS);
        showFragment();
    }

    private void initData() {
        currIndex = 0;
        fragmentTags = new ArrayList<>(Arrays.asList("HomeFragment", "ImFragment", "InterestFragment", "MemberFragment"));
    }

    private void initView() {
        group = (RadioGroup) findViewById(R.id.group);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.foot_bar_home: currIndex = 0; break;
                    case R.id.foot_bar_im: currIndex = 1; break;
                    case R.id.foot_bar_interest: currIndex = 2; break;
                    case R.id.main_footbar_user: currIndex = 3; break;
                    default: break;
                }
                showFragment();
            }
        });
        showFragment();
    }

    private void showFragment() {
        if (currIndex == 3) {
            UIHelper.showLogin(MainActivity.this);
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentTags.get(currIndex));
        if(fragment == null) {
            fragment = instantFragment(currIndex);
        }
        for (int i = 0; i < fragmentTags.size(); i++) {
            Fragment f = fragmentManager.findFragmentByTag(fragmentTags.get(i));
            if(f != null && f.isAdded()) {
                fragmentTransaction.hide(f);
            }
        }
        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.fragment_container, fragment, fragmentTags.get(currIndex));
        }
        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    private Fragment instantFragment(int currIndex) {
        switch (currIndex) {
            case 0: return new MainPagerFragment();
            case 1: return new WildebeestFragment();
//            case 2: return new BufferKnifeFragment();
            case 2: return new SensorFragment();
            case 3: return new MemberFragment();
            default: return null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_OPEN_STATUS:
                Boolean isOpen = CommUtil.isOpenGPS(this);
                if (isOpen) {
                    showToast("已开启GPS！");
                    startLocService();
                } else {
                    showToast("请开启GPS！");
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, GPS_OPEN_STATUS);
                }
                break;
        }
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
