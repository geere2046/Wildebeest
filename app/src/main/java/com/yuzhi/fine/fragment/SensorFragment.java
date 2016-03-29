package com.yuzhi.fine.fragment;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yuzhi.fine.R;
import com.yuzhi.fine.activity.MainActivity;
import com.yuzhi.fine.ui.loadmore.LoadMoreListView;
import com.yuzhi.fine.ui.quickadapter.BaseAdapterHelper;
import com.yuzhi.fine.ui.quickadapter.QuickAdapter;
import com.yuzhi.fine.utils.DeviceUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

/**
 * Created by huangyc on 2016/3/9.
 */
public class SensorFragment extends Fragment {

    String TAG = SensorFragment.class.getSimpleName();
    Context context;
    QuickAdapter<Sensor> adapter;
    @Bind(R.id.listView)
    LoadMoreListView listView;
    @Bind(R.id.rotate_header_list_view_frame)
    PtrClassicFrameLayout mPtrFrame;
    List<Sensor> listSensor;

    public SensorFragment() {
        super();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demo_ptr, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (MainActivity) getActivity();
        initData();
        initView();
        loadData();
    }

    void initData() {
        SensorManager sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    void initView() {
        adapter = new QuickAdapter<Sensor>(context, R.layout.recommend_shop_list_item) {
            protected void convert(BaseAdapterHelper helper, Sensor item) {
                helper.setText(R.id.name, item.getName()).setText(R.id.address, matchDesc(item.getType()))
                        .setImageResource(R.id.logo, matchIcon(item.getType()));
            }
        };
        listView.setDrawingCacheEnabled(true);
        listView.setAdapter(adapter);

        // header custom begin
        final StoreHouseHeader header = new StoreHouseHeader(context);
        header.setPadding(0, DeviceUtil.dp2px(context, 15), 0, 0);
        header.initWithString("Fine");
        header.setTextColor(getResources().getColor(R.color.gray));
        mPtrFrame.setHeaderView(header);
        mPtrFrame.addPtrUIHandler(header);
        // header custom end

        // 下拉刷新
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                initData();
                loadData();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });

        // 加载更多
        listView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData();
            }
        });

        // 点击事件
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,">>>>>>>>>>>>>>> "+i);
                showDetail(i);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    Picasso.with(context).pauseTag(context);
                } else {
                    Picasso.with(context).resumeTag(context);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
    }

    void loadData() {
        listView.setLoadMoreViewTextNoMoreData();
        mPtrFrame.refreshComplete();
        adapter.clear();
        adapter.addAll(listSensor);
    }

    String matchDesc(int type) {
        String id;
        switch (type) {
            case 1:
                id = getResources().getString(R.string.sensor_desc_1);
                break;
            case 2:
                id = getResources().getString(R.string.sensor_desc_2);
                break;
            case 3:
                id = getResources().getString(R.string.sensor_desc_3);
                break;
            case 4:
                id = getResources().getString(R.string.sensor_desc_4);
                break;
            case 5:
                id = getResources().getString(R.string.sensor_desc_5);
                break;
            case 6:
                id = getResources().getString(R.string.sensor_desc_6);
                break;
            case 7:
                id = getResources().getString(R.string.sensor_desc_13);
                break;
            case 8:
                id = getResources().getString(R.string.sensor_desc_8);
                break;
            case 9:
                id = getResources().getString(R.string.sensor_desc_9);
                break;
            case 10:
                id = getResources().getString(R.string.sensor_desc_10);
                break;
            case 11:
                id = getResources().getString(R.string.sensor_desc_11);
                break;
            case 12:
                id = getResources().getString(R.string.sensor_desc_12);
                break;
            case 13:
                id = getResources().getString(R.string.sensor_desc_13);
                break;
            default:
                id = "";
                break;
        }
        return id;
    }

    int matchIcon(int type) {
        int id = 0;
        switch (type) {
            case 1:
                id = R.drawable.ic_sensor_1;
                break;
            case 2:
                id = R.drawable.ic_sensor_2;
                break;
            case 3:
                id = R.drawable.ic_sensor_3;
                break;
            case 4:
                id = R.drawable.ic_sensor_4;
                break;
            case 5:
                id = R.drawable.ic_sensor_5;
                break;
            case 6:
                id = R.drawable.ic_sensor_6;
                break;
            case 7:
                id = R.drawable.ic_sensor_13;
                break;
            case 8:
                id = R.drawable.ic_sensor_8;
                break;
            case 9:
                id = R.drawable.ic_sensor_9;
                break;
            case 10:
                id = R.drawable.ic_sensor_10;
                break;
            case 11:
                id = R.drawable.ic_sensor_11;
                break;
            case 12:
                id = R.drawable.ic_sensor_12;
                break;
            case 13:
                id = R.drawable.ic_sensor_13;
                break;
            default:
                break;
        }
        return id;
    }

    void showDetail(int i) {
        Sensor sensor = listSensor.get(i);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.verbose, null);
        ((TextView) layout.findViewById(R.id.vendor_value)).setText(sensor
                .getVendor());
        ((TextView) layout.findViewById(R.id.power_value)).setText(sensor
                .getPower() + getString(R.string.unit_consumption));
        ((TextView) layout.findViewById(R.id.resolution_value)).setText(sensor
                .getResolution() + "");
        ((TextView) layout.findViewById(R.id.version_value)).setText(sensor
                .getVersion() + "");
        ((TextView) layout.findViewById(R.id.delay_value)).setText(sensor
                .getMinDelay() + getString(R.string.unit_mindelay));
        ((TextView) layout.findViewById(R.id.range_value)).setText(sensor
                .getMaximumRange() + "");
        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(layout);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(sensor.getName());
        int imageResource = R.drawable.ic_sensor_unknown;
        try {
            imageResource = getResources().getIdentifier(
                    "drawable/ic_sensor_" + sensor.getType(), null,
                    "de.onyxbits.sensorreadout");
            if (imageResource == 0) {
                imageResource = R.drawable.ic_sensor_unknown;
            }
        } catch (Exception e) {
        }
        builder.setIcon(imageResource);
        builder.setView(scrollView);
        builder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Picasso.with(context).resumeTag(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        Picasso.with(context).pauseTag(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Picasso.with(context).cancelTag(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
