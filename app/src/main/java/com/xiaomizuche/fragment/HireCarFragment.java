package com.xiaomizuche.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomizuche.R;
import com.xiaomizuche.activity.AroundCarsActivity;
import com.xiaomizuche.activity.BatteryActivity;
import com.xiaomizuche.activity.HomeActivity;
import com.xiaomizuche.activity.LoginActivity;
import com.xiaomizuche.activity.ManageCardActivity;
import com.xiaomizuche.activity.MyCardActivity;
import com.xiaomizuche.activity.SimpleNaviActivity;
import com.xiaomizuche.base.BaseActivity;
import com.xiaomizuche.base.BaseFragment;
import com.xiaomizuche.bean.CarRecordBean;
import com.xiaomizuche.bean.LocInfoBean;
import com.xiaomizuche.bean.ResponseBean;
import com.xiaomizuche.bean.TrackBean;
import com.xiaomizuche.bean.UserInfoBean;
import com.xiaomizuche.bean.ValidateCodeBean;
import com.xiaomizuche.callback.DCommonCallback;
import com.xiaomizuche.callback.DSingleDialogCallback;
import com.xiaomizuche.constants.AppConfig;
import com.xiaomizuche.constants.JCConstValues;
import com.xiaomizuche.db.ConfigService;
import com.xiaomizuche.event.ChangeLocationEvent;
import com.xiaomizuche.event.RemoteLockCarEvent;
import com.xiaomizuche.event.RemoteVFEvent;
import com.xiaomizuche.event.StopNaviEvent;
import com.xiaomizuche.http.DHttpUtils;
import com.xiaomizuche.http.DMethods;
import com.xiaomizuche.http.DRequestParamsUtils;
import com.xiaomizuche.http.HttpConstants;
import com.xiaomizuche.map.JCLocationManager;
import com.xiaomizuche.map.TTSController;
import com.xiaomizuche.utils.CommonUtils;
import com.xiaomizuche.utils.MapUtils;
import com.xiaomizuche.utils.T;
import com.xiaomizuche.utils.Utils;
import com.xiaomizuche.view.CustomDialog;
import com.xiaomizuche.view.TopBarView;
import com.xiaomizuche.view.formview.FormTextDateTimeView;
import com.xiaomizuche.view.formview.FormViewUtils;

import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * 租车
 */
public class HireCarFragment extends BaseFragment implements TextWatcher, Runnable, View.OnClickListener,
        AMap.OnMapClickListener, AMapNaviListener, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, GeocodeSearch.OnGeocodeSearchListener {

    @ViewInject(R.id.top_bar_view)
    TopBarView topBarView;
    @ViewInject(R.id.tv_expire)
    TextView expireView;
    @ViewInject(R.id.fl_location)
    FrameLayout locationLayout;
    @ViewInject(R.id.ll_hire_car)
    LinearLayout hireCarLayout;
    @ViewInject(R.id.iv_car_id)
    ImageView carIdView;
    @ViewInject(R.id.et_car_id)
    EditText carIdText;
    @ViewInject(R.id.v_line_car_id)
    View carIdLine;
    @ViewInject(R.id.iv_validatecode)
    ImageView validatecodeView;
    @ViewInject(R.id.et_validatecode)
    EditText validatecodeText;
    @ViewInject(R.id.v_line_validatecode)
    View validatecodeLine;
    @ViewInject(R.id.tv_lock)
    TextView lockView;
    @ViewInject(R.id.tv_send_validatecode)
    TextView sendValidatecodeView;

    @ViewInject(R.id.map_view)
    MapView mapView;
    @ViewInject(R.id.iv_traffic)
    ImageView trafficImageView;
    @ViewInject(R.id.iv_map_type)
    ImageView mapTypeImageView;
    @ViewInject(R.id.iv_trajectory)
    ImageView trajectoryImageView;
    @ViewInject(R.id.iv_fence)
    ImageView fenceImageView;
    @ViewInject(R.id.iv_car_status)
    ImageView carStatusImageView;
    @ViewInject(R.id.iv_battery)
    ImageView batteryView;
    @ViewInject(R.id.iv_nav)
    ImageView navImageView;

    //轨迹查询时间布局
    LinearLayout dateLayout;
    //轨迹查询开始时间
    FormTextDateTimeView startDateView;
    //轨迹查询结束时间
    FormTextDateTimeView endDateView;
    private AMap aMap;
    private UiSettings uiSettings;
    //标志位，标志已经初始化完成
    private boolean isPrepared;
    private Handler handler = new Handler();
    private boolean hasTrack;
    //车辆位置信息
    private LocInfoBean locInfoBean;
    //手动选择起点
    private boolean _isChooseStart;
    //手动选择起点的信息框
    private Marker _startMarker;
    //电动车上面信息框
    private Marker marker;
    //是否开启实时路况
    private boolean isOpenTraffic;
    //地图类型选择窗口
    private View mapTypeView;
    TextView satelliteTextView;
    TextView planeTextView;
    private PopupWindow popupWindow;
    //车辆信息
    private View carStatusView;
    TextView startTimeView;
    TextView expireTimeView;
    TextView equipmentSerialNumberTextView;
    TextView positioningStateTextView;
    TextView onlineStatusTextView;
    TextView accTextView;
    TextView mainPowerTextView;
    TextView lockCarStatusTextView;
    TextView directionTextView;
    TextView addressTextView;
    private PopupWindow carPopupWindow;
    //其他
    private MediaPlayer _dingPlayer = null;
    private Timer _dingPlayerTimer = null;
    private int _dingPlayerTimerCount = 0;
    private MediaPlayer _ding2Player = null;
    private Timer _ding2PlayerTimer = null;
    private int _ding2PlayerTimerCount = 0;
    //电子围栏覆盖物
    private Circle circle;
    //地图导航类
    private AMapNavi mapNavi;
    //电动车上的信息框显示状态(默认显示)
    private boolean isHidden;

    private View chooseNavView;
    private TextView driveView;
    private TextView walkView;

    private Handler countHandler;
    private Runnable countRunnable;
    private Handler sendHandler;
    private Runnable sendRunnable;
    private Handler backHandler;
    private Runnable backRunnable;
    private int minute = 60;
    private String validateCode;
    private ValidateCodeBean validateCodeBean;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hire_car, container, false);
        x.view().inject(this, view);
        initMapType(inflater);
        initCarStatus(inflater);
        mapView.onCreate(savedInstanceState);
        init();
        setListeners();

        isPrepared = true;
        EventBus.getDefault().register(this);
        return view;
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);
            aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
            aMap.setOnInfoWindowClickListener(this);
            uiSettings = aMap.getUiSettings();
            //不显示缩放按键
            uiSettings.setZoomControlsEnabled(false);
            //显示比例尺
            uiSettings.setScaleControlsEnabled(true);
        }
        moveToCenter("36.68445", "117.126229");
        JCLocationManager.instance().init(getActivity());
        // 初始化语音模块
        TTSController ttsManager = TTSController.getInstance(getActivity());
        ttsManager.init();
        mapNavi = AMapNavi.getInstance(getActivity());
        mapNavi.setAMapNaviListener(ttsManager);// 设置语音模块播报
        mapNavi.setAMapNaviListener(this);
    }

    private void initMapType(LayoutInflater inflater) {
        mapTypeView = inflater.inflate(R.layout.popupwindow_map_type, null, false);
        satelliteTextView = (TextView) mapTypeView.findViewById(R.id.tv_satellite);
        planeTextView = (TextView) mapTypeView.findViewById(R.id.tv_plane);
    }

    private void initCarStatus(LayoutInflater inflater) {
        carStatusView = inflater.inflate(R.layout.popupwindow_car_status, null, false);
        carStatusView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        startTimeView = (TextView) carStatusView.findViewById(R.id.tv_start_time);
        expireTimeView = (TextView) carStatusView.findViewById(R.id.tv_expire_time);
        equipmentSerialNumberTextView = (TextView) carStatusView.findViewById(R.id.tv_equipment_serial_number);
        positioningStateTextView = (TextView) carStatusView.findViewById(R.id.tv_positioning_state);
        onlineStatusTextView = (TextView) carStatusView.findViewById(R.id.tv_online_status);
        accTextView = (TextView) carStatusView.findViewById(R.id.tv_acc);
        mainPowerTextView = (TextView) carStatusView.findViewById(R.id.tv_main_power);
        lockCarStatusTextView = (TextView) carStatusView.findViewById(R.id.tv_lock_car_status);
        directionTextView = (TextView) carStatusView.findViewById(R.id.tv_direction);
        addressTextView = (TextView) carStatusView.findViewById(R.id.tv_address);
    }

    //设置地图中心点
    private void moveToCenter(String lat, String lng) {
        String centerLatLng = ConfigService.instance().getConfigValue(
                JCConstValues.S_CenterLngLat);
        if (centerLatLng.length() == 0) {
            centerLatLng = lat + "," + lng;
            ConfigService.instance().insertConfigValue(
                    JCConstValues.S_CenterLngLat, centerLatLng);
        }
        String[] latlngArr = centerLatLng.split(",");
        if (latlngArr.length < 2) {
            latlngArr[0] = lat;
            latlngArr[1] = lng;
            centerLatLng = lat + "," + lng;
            ConfigService.instance().insertConfigValue(
                    JCConstValues.S_CenterLngLat, centerLatLng);
        }
        CameraPosition cp = new CameraPosition(
                new LatLng(Double.valueOf(latlngArr[0]),
                        Double.valueOf(latlngArr[1])), 17, 0, 0);
        CameraUpdate center = CameraUpdateFactory.newCameraPosition(cp);
        aMap.moveCamera(center);
    }

    private void setListeners() {
        trafficImageView.setOnClickListener(this);
        mapTypeImageView.setOnClickListener(this);
        trajectoryImageView.setOnClickListener(this);
        fenceImageView.setOnClickListener(this);
        navImageView.setOnClickListener(this);
        satelliteTextView.setOnClickListener(this);
        planeTextView.setOnClickListener(this);
        carStatusImageView.setOnClickListener(this);
        batteryView.setOnClickListener(this);
        carIdText.addTextChangedListener(this);
        validatecodeText.addTextChangedListener(this);
    }

    private void showCountDown() {
        countHandler = new Handler();
        countRunnable = new Runnable() {
            @Override
            public void run() {
                topBarView.setRightTextView(CommonUtils.DateMinus(AppConfig.userInfoBean.getCarRecord().getExpectEndTime()));
                countHandler.postDelayed(this, 60 * 1000);
            }
        };

        countHandler.postDelayed(countRunnable, 0);
    }

    @Override
    public void requestDatas() {
        if (!isPrepared || !isVisible || hasLoadedOnce || !isAdded()) {
            return;
        }
        if (AppConfig.userInfoBean != null && AppConfig.userInfoBean.getCarRecord() != null) {
            if (hasTrack) {
                aMap.clear();
            }
            topBarView.setCenterTextView("车辆控制");
            hireCarLayout.setVisibility(View.GONE);
            locationLayout.setVisibility(View.VISIBLE);
            showCountDown();
            startTimeView.setText(AppConfig.userInfoBean.getCarRecord().getStartTime());
            expireTimeView.setText(AppConfig.userInfoBean.getCarRecord().getExpectEndTime());
            RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getLocInfoUrl());
            DHttpUtils.get_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    ResponseBean<LocInfoBean> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<LocInfoBean>>() {
                    }.getType());
                    if (responseBean != null && responseBean.getCode() == 1) {
                        locInfoBean = responseBean.getData();
                        if (locInfoBean.getLon() > 0 && locInfoBean.getLat() > 0) {
                            //清除之前添加的障碍物
                            if (marker != null) {
                                marker.remove();
                            }
                            //添加障碍物
                            MarkerOptions markerOptions = new MarkerOptions();
                            LatLng position = new LatLng(locInfoBean.getLat() / 1000000.0, locInfoBean.getLon() / 1000000.0);
                            markerOptions.position(position);
                            markerOptions.title(locInfoBean.getSatelliteTime()).snippet(locInfoBean.getSpeed() + "km/h");
                            markerOptions.perspective(true);
                            if (locInfoBean.getIsOnline().equals("1")) {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_ebike_online));
                            } else {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_ebike_offline));
                            }
                            marker = aMap.addMarker(markerOptions);
                            if (!isHidden) {
                                marker.showInfoWindow();
                            } else {
                                marker.hideInfoWindow();
                            }
                            CameraUpdate cu = CameraUpdateFactory.changeLatLng(position);
                            aMap.moveCamera(cu);
                            //判断锁车状态
                            if (AppConfig.isExecuteLock == null) {
                                if (locInfoBean.getLock().equals("1")) {
                                    AppConfig.isLock = true;
                                    lockView.setText("解锁");
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_unlock);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                                    lockView.setCompoundDrawables(drawable, null, null, null);
                                } else {
                                    AppConfig.isLock = false;
                                    lockView.setText("锁车");
                                    Drawable drawable = getResources().getDrawable(R.mipmap.icon_lock);
                                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                                    lockView.setCompoundDrawables(drawable, null, null, null);
                                }
                            }
                            //判断电子围栏
                            if (AppConfig.isExecuteVF == null) {
                                if (locInfoBean.isOpenVf()) {
                                    fenceImageView.setImageResource(R.mipmap.icon_fence_open);
                                    LatLng vfPosition = new LatLng(locInfoBean.getVfLat() / 1000000.0, locInfoBean.getVfLon() / 1000000.0);
                                    if (circle == null) {
                                        circle = aMap.addCircle(new CircleOptions().center(vfPosition)
                                                .radius(100).strokeColor(Color.RED).fillColor(Color.TRANSPARENT)
                                                .strokeWidth(5));
                                    }
                                } else {
                                    fenceImageView.setImageResource(R.mipmap.icon_fence_close);
                                    if (circle != null) {
                                        circle.remove();
                                        circle = null;
                                    }
                                }
                            }
                        } else {
                            showShortText("定位失败");
                        }
                        //刷新车辆信息
                        equipmentSerialNumberTextView.setText(locInfoBean.getCarId() + "");
                        if (locInfoBean.getSourceType() == 1) {
                            positioningStateTextView.setText("GPS定位");
                        } else if (locInfoBean.getSourceType() == 2) {
                            positioningStateTextView.setText("基站定位");
                        }
                        if (locInfoBean.getIsOnline().equals("1")) {
                            onlineStatusTextView.setText("在线");
                        } else {
                            onlineStatusTextView.setText("离线");
                        }
                        if (locInfoBean.getAcc().equals("1")) {
                            accTextView.setText("开启");
                        } else {
                            accTextView.setText("关闭");
                        }
                        if (locInfoBean.getPower().equals("1")) {
                            mainPowerTextView.setText("开启");
                        } else {
                            mainPowerTextView.setText("关闭");
                        }
                        if (locInfoBean.getLock().equals("1")) {
                            lockCarStatusTextView.setText("锁定");
                        } else {
                            lockCarStatusTextView.setText("未锁定");
                        }
                        directionTextView.setText(MapUtils.directionStr(locInfoBean.getHeading()));
                        GeocodeSearch geocodeSearch = new GeocodeSearch(getActivity());
                        geocodeSearch.setOnGeocodeSearchListener(HireCarFragment.this);
                        LatLonPoint latLonPoint = new LatLonPoint(locInfoBean.getLat() / 1000000.0, locInfoBean.getLon() / 1000000.0);
                        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
                        geocodeSearch.getFromLocationAsyn(regeocodeQuery);
                    } else {
                        showShortText(responseBean.getErrmsg());
                    }
                }
            });
        } else {
            topBarView.setRightTextView("");
            topBarView.setCenterTextView("申请用车");
            hireCarLayout.setVisibility(View.VISIBLE);
            locationLayout.setVisibility(View.GONE);
            if (AppConfig.userInfoBean != null
                    && !CommonUtils.strIsEmpty(AppConfig.userInfoBean.getExpireTime())
                    && CommonUtils.moreThanToday(AppConfig.userInfoBean.getExpireTime())) {
                expireView.setVisibility(View.VISIBLE);
            } else {
                expireView.setVisibility(View.INVISIBLE);
            }
            if (countHandler != null) {
                countHandler.removeCallbacks(countRunnable);
            }
        }
    }

    @Event(value = R.id.tv_expire)
    private void expire(View v) {
        startActivity(new Intent(getActivity(), MyCardActivity.class));
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (hasTrack) {
            aMap.clear();
            //每30秒刷新一次电动车位置
            handler.postDelayed(this, 1000 * 30);
            hasTrack = false;
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        TTSController.getInstance(getActivity()).startSpeaking();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void run() {
        requestDatas();
        handler.postDelayed(this, 1000 * 30);// 间隔30秒
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_traffic://实时路况
                if (isOpenTraffic) {
                    isOpenTraffic = false;
                    trafficImageView.setImageResource(R.mipmap.icon_traffic_close);
                } else {
                    isOpenTraffic = true;
                    trafficImageView.setImageResource(R.mipmap.icon_traffic_open);
                }
                aMap.setTrafficEnabled(isOpenTraffic);
                break;
            case R.id.iv_map_type://切换地图类型
                if (popupWindow == null) {
                    popupWindow = CommonUtils.createPopupWindow(mapTypeView);
                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            mapTypeImageView.setImageResource(R.mipmap.map_switch);
                        }
                    });
                }
                popupWindow.showAsDropDown(mapTypeImageView);
                mapTypeImageView.setImageResource(R.mipmap.close_map_type_tip);
                break;
            case R.id.tv_satellite://卫星图
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                changeMapType(AMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.tv_plane://平面图
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                changeMapType(AMap.MAP_TYPE_NORMAL);
                break;
            case R.id.iv_trajectory://轨迹
                //选择时间弹出框
                initDateWindow();
                CommonUtils.showCustomDialog1(getActivity(), "", dateLayout, new DSingleDialogCallback() {
                    @Override
                    public void onPositiveButtonClick(String editText) {
                        // 发送命令到服务器，在获取了轨迹信息之后，展现在地图上
                        String startTime = startDateView.getDateText();
                        String endTime = endDateView.getDateText();
                        if (CommonUtils.moreThanAWeek(startTime, endTime)) {
                            showShortText("最多可查看一周的轨迹，您已超出时间范围，请修改后重试。");
                            return;
                        }
                        RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getTrackInfoUrl(startTime, endTime));
                        DHttpUtils.get_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                ResponseBean<List<TrackBean>> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<List<TrackBean>>>() {
                                }.getType());
                                if (responseBean != null && responseBean.getCode() == 1) {
                                    hasTrack = true;
                                    aMap.clear();
                                    if (responseBean.getData().size() <= 0) {
                                        return;
                                    }
                                    List<LatLng> points = new ArrayList<LatLng>();
                                    int i = 0;
                                    int maxCount = responseBean.getData().size();
                                    for (TrackBean trackBean : responseBean.getData()) {
                                        LatLng point = new LatLng(trackBean.getLat() / 1000000.0,
                                                trackBean.getLon() / 1000000.0);
                                        points.add(point);
                                        if (i != 0 && i < maxCount - 1) {
                                            addMarkerToMap(point,
                                                    getResources().getString(R.string.txt_track_prompt),
                                                    trackBean.getSatelliteTime(), trackBean.getSpeed(),
                                                    R.mipmap.point, false);
                                        }
                                        i++;
                                    }
                                    TrackBean start = responseBean.getData().get(0);
                                    TrackBean end = responseBean.getData().get(maxCount - 1);
                                    addMarkerToMap(points.get(0),
                                            getResources().getString(R.string.txt_start),
                                            start.getSatelliteTime(), start.getSpeed(),
                                            R.mipmap.marker_start, true);
                                    if (maxCount > 1) {
                                        addMarkerToMap(points.get(points.size() - 1), getResources()
                                                        .getString(R.string.txt_end), end.getSatelliteTime(),
                                                end.getSpeed(), R.mipmap.marker_end, true);
                                    }
                                    //在地图上添加轨迹实线
                                    drawTrack(points);
                                    CameraPosition cp = new CameraPosition(points.get(0), 17, 0, 0);
                                    CameraUpdate center = CameraUpdateFactory.newCameraPosition(cp);
                                    aMap.moveCamera(center);
                                    handler.removeCallbacks(HireCarFragment.this); //停止刷新
                                } else {
                                    showShortText(responseBean.getErrmsg());
                                }
                            }
                        });

                    }
                });
                break;
            case R.id.iv_fence://电子围栏
                if (AppConfig.isExecuteVF != null) {
                    if (AppConfig.isExecuteVF == 1) {
                        CommonUtils.showCustomDialogSignle3(getActivity(), "", "正在打开电子围栏，请稍等。");
                    } else if (AppConfig.isExecuteVF == 0) {
                        CommonUtils.showCustomDialogSignle3(getActivity(), "", "正在关闭电子围栏，请稍等。");
                    }
                } else {
                    if (locInfoBean.isOpenVf()) {
                        CommonUtils.showCustomDialog0(getActivity(), "提示", "您确定要关闭电子围栏吗？", new DSingleDialogCallback() {
                            @Override
                            public void onPositiveButtonClick(String editText) {
                                RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getCloseVfUrl());
                                DHttpUtils.get_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                        ResponseBean<String> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<String>>() {
                                        }.getType());
                                        if (responseBean != null) {
                                            AppConfig.isExecuteVF = 0;
                                            showShortText(responseBean.getErrmsg());
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        //打开电子围栏前需切回定位模式
                        onEvent(new ChangeLocationEvent(true));
                        CommonUtils.showCustomDialog0(getActivity(), "提示", "您确定要开启电子围栏吗？", new DSingleDialogCallback() {
                            @Override
                            public void onPositiveButtonClick(String editText) {
                                //常数
                                int r = 100;
                                int K = 111700 * 2;
                                double R = 3.141592654 / 180;
                                double latitude = locInfoBean.getLat() / 1000000.0;
                                double longitude = locInfoBean.getLon() / 1000000.0;
                                //计算电子围栏的范围
                                double maxLat = latitude + (double) r / K;//最大纬度
                                double minLat = latitude - (double) r / K;//最小纬度
                                double minLon = longitude - r / (K * Math.cos(latitude * R));//最小经度
                                double maxLon = longitude + r / (K * Math.cos(latitude * R));//最大经度
                                RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getOpenVfUrl(longitude, latitude, maxLon, maxLat, minLon, minLat));
                                DHttpUtils.get_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                        ResponseBean<String> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<String>>() {
                                        }.getType());
                                        if (responseBean != null) {
                                            AppConfig.isExecuteVF = 1;
                                            showShortText(responseBean.getErrmsg());
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
                break;
            case R.id.iv_car_status://车辆信息
                if (carPopupWindow == null) {
                    carPopupWindow = CommonUtils.createAbovePopupWindow(carStatusView);
                    carPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            carStatusImageView.setImageResource(R.mipmap.icon_car_status);
                        }
                    });
                }
                int popupWidth = carStatusView.getMeasuredWidth();
                int popupHeight = carStatusView.getMeasuredHeight();
                int[] vLocation = new int[2];
                v.getLocationOnScreen(vLocation);
                carPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (vLocation[0] + v.getWidth() / 2) - popupWidth / 2,
                        vLocation[1] - popupHeight);
                carStatusImageView.setImageResource(R.mipmap.close_car_status_tip);
                break;
            case R.id.iv_battery://电量
                if (locInfoBean != null && locInfoBean.getUpVoltage() != null
                        && locInfoBean.getUpVoltage().equals("2")) {
                    Intent intent1 = new Intent(getActivity(), BatteryActivity.class);
                    intent1.putExtra("CurrVoltage", locInfoBean.getVoltage());
                    intent1.putExtra("RemainBattery", locInfoBean.getRemainBattery());
                    startActivity(intent1);
                } else {
                    showShortText("您的设备不支持此功能");
                }
                break;
            case R.id.iv_nav://导航
                marker.hideInfoWindow();
                JCLocationManager.instance().start();
                Location location = JCLocationManager.instance().getCurrentLocation();
                LatLonPoint destPoint = new LatLonPoint(locInfoBean.getLat() / 1000000.0, locInfoBean.getLon() / 1000000.0);
                if (location != null) {
                    LatLonPoint startPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
                    chooseNav(startPoint, destPoint);
                } else {
                    String text = "手机定位尚未完成，请先在地图上选择您的位置";
                    TTSController.getInstance(getActivity()).playText(text);
                    showShortText(text);
                    _isChooseStart = true;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                            new LatLng(destPoint.getLatitude(), destPoint.getLongitude()), 15);
                    aMap.moveCamera(cu);
                }
                break;
        }
    }

    private void chooseNav(final LatLonPoint startPoint, final LatLonPoint destPoint) {
        chooseNavView = LayoutInflater.from(getActivity()).inflate(R.layout.view_choose_nav, null, false);
        driveView = (TextView) chooseNavView.findViewById(R.id.tv_drive);
        walkView = (TextView) chooseNavView.findViewById(R.id.tv_walk);
        final CustomDialog dialog = CommonUtils.showCustomDialog1(getActivity(), "选择导航方式", chooseNavView);
        driveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startNavi(startPoint, destPoint, "1");
            }
        });
        walkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                startNavi(startPoint, destPoint, "2");
            }
        });
    }

    //切换地图类型时变化页面UI
    private void changeMapType(int type) {
        if (type == AMap.MAP_TYPE_SATELLITE) {
            //选中卫星图
            Drawable satelliteDrawable = getResources().getDrawable(R.mipmap.satellite_hover);
            satelliteDrawable.setBounds(0, 0, satelliteDrawable.getMinimumWidth(), satelliteDrawable.getMinimumHeight());
            satelliteTextView.setCompoundDrawables(null, satelliteDrawable, null, null);
            //还原平面图
            Drawable planeDrawable = getResources().getDrawable(R.mipmap.plane);
            planeDrawable.setBounds(0, 0, planeDrawable.getMinimumWidth(), planeDrawable.getMinimumHeight());
            planeTextView.setCompoundDrawables(null, planeDrawable, null, null);
        } else if (type == AMap.MAP_TYPE_NORMAL) {
            //选中平面图
            Drawable planeDrawable = getResources().getDrawable(R.mipmap.plane_hover);
            planeDrawable.setBounds(0, 0, planeDrawable.getMinimumWidth(), planeDrawable.getMinimumHeight());
            planeTextView.setCompoundDrawables(null, planeDrawable, null, null);
            //选中卫星图
            Drawable satelliteDrawable = getResources().getDrawable(R.mipmap.satellite);
            satelliteDrawable.setBounds(0, 0, satelliteDrawable.getMinimumWidth(), satelliteDrawable.getMinimumHeight());
            satelliteTextView.setCompoundDrawables(null, satelliteDrawable, null, null);
        }
    }


    //在地图上添加轨迹蓝点
    private Marker addMarkerToMap(LatLng latlng, String devName, String time,
                                  double speed, int drawableId, boolean isPerspective) {
        MarkerOptions options = new MarkerOptions();
        options.position(latlng);
        options.title(devName);
        if (drawableId == R.mipmap.marker_start || drawableId == R.mipmap.marker_end) {
            options.anchor(0.5f, 1.0f);
        } else {
            options.anchor(0.5f, 0.5f);
        }
        options.snippet(CommonUtils.DateToString(new Date(time), "yyyy-MM-dd HH:mm:ss") + "\n" + String.valueOf(speed) + "km/h");
        options.draggable(false);
        BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(drawableId);
        options.icon(bd);
        return aMap.addMarker(options);
    }

    //在地图上添加轨迹实线
    private void drawTrack(List<LatLng> points) {
        PolylineOptions options = new PolylineOptions();
        options.addAll(points);
        options.color(Color.argb(255, 47, 172, 245));
        aMap.addPolyline(options);
    }

    //选择时间弹出框
    private void initDateWindow() {
        //时间布局
        dateLayout = new LinearLayout(getActivity());
        dateLayout.setOrientation(LinearLayout.VERTICAL);
        dateLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //开始时间
        startDateView = FormViewUtils.createFormTextDateTimeView(getActivity(), "开始时间", "请选择开始时间", 2, true, true, true);
        startDateView.setDateText(CommonUtils.getYesterdayDateString("yyyy-MM-dd HH:mm"));
        startDateView.setmFormViewOnclick(true);
        dateLayout.addView(startDateView);
        //结束时间
        endDateView = FormViewUtils.createFormTextDateTimeView(getActivity(), "结束时间", "请选择结束时间", 2, true, true, true);
        endDateView.setDateText(CommonUtils.getCurrentDateString("yyyy-MM-dd HH:mm"));
        endDateView.setmFormViewOnclick(true);
        dateLayout.addView(endDateView);
    }

    //处理远程锁车推送
    public void onEvent(RemoteLockCarEvent event) {
        if (event != null) {
            AppConfig.isExecuteLock = null;
            if (event.getIsLock().equals("1")) {
                AppConfig.isLock = true;
                lockView.setText("解锁");
                Drawable drawable = getResources().getDrawable(R.mipmap.icon_unlock);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                lockView.setCompoundDrawables(drawable, null, null, null);
                showShortText(event.getMsg());
            } else {
                AppConfig.isLock = false;
                lockView.setText("锁车");
                Drawable drawable = getResources().getDrawable(R.mipmap.icon_lock);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                lockView.setCompoundDrawables(drawable, null, null, null);
                showShortText(event.getMsg());
            }
        }
    }

    //处理电子围栏推送
    public void onEvent(RemoteVFEvent event) {
        if (event != null) {
            if (event.getIsOpen().equals("1")) {
                fenceImageView.setImageResource(R.mipmap.icon_fence_open);
                LatLng position = new LatLng(locInfoBean.getLat() / 1000000.0, locInfoBean.getLon() / 1000000.0);
                if (circle == null) {
                    circle = aMap.addCircle(new CircleOptions().center(position)
                            .radius(100).strokeColor(Color.RED).fillColor(Color.TRANSPARENT)
                            .strokeWidth(5));
                }
                locInfoBean.setIsOpenVf(true);
            } else {
                fenceImageView.setImageResource(R.mipmap.icon_fence_close);
                if (circle != null) {
                    circle.remove();
                    circle = null;
                    locInfoBean.setIsOpenVf(false);
                }
            }
            AppConfig.isExecuteVF = null;
            showShortText(event.getMsg());
        }
    }

    //停止导航
    public void onEvent(StopNaviEvent event) {
        if (event != null && event.isStopNavi()) {
            this.stopNavi();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (_isChooseStart) {
            if (_startMarker != null) {
                _startMarker.remove();
            }
            _startMarker = aMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 1)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.mipmap.point)).position(latLng)
                    .snippet("点击选择为起点"));
            _startMarker.showInfoWindow();
        }
    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation newLoc) {
        double d = calculateDistance(newLoc.getCoord().getLatitude(), newLoc.getCoord().getLongitude()
                , locInfoBean.getLat() / 1000000.0f
                , locInfoBean.getLon() / 1000000.0f);
        checkToPlayDing(d);
    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {
        Intent intent = new Intent(getActivity(),
                SimpleNaviActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Bundle bundle = new Bundle();
        bundle.putInt(Utils.ACTIVITYINDEX, Utils.SIMPLEGPSNAVI);
        bundle.putBoolean(Utils.ISEMULATOR, false);
        intent.putExtras(bundle);
        startActivity(intent);
        ((HomeActivity) getActivity()).dismissLoadingprogress();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        ((HomeActivity) getActivity()).startLoadingProgress();
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    //开始导航
    private void startNavi(LatLonPoint startPoint, LatLonPoint stopPoint, String type) {
        NaviLatLng startP = new NaviLatLng(startPoint.getLatitude(), startPoint.getLongitude());
        NaviLatLng endP = new NaviLatLng(stopPoint.getLatitude(), stopPoint.getLongitude());
        List<NaviLatLng> startList = new ArrayList<>();
        startList.add(startP);
        List<NaviLatLng> endList = new ArrayList<>();
        endList.add(endP);
        if (type.equals("1")) {
            AMapNavi.getInstance(getActivity()).calculateDriveRoute(startList, endList, null, AMapNavi.DrivingDefault);
        } else if (type.equals("2")) {
            boolean flag = AMapNavi.getInstance(getActivity()).calculateWalkRoute(startP, endP);
            if (!flag) {
                Toast.makeText(getActivity(), "您距电动车距离较远，将为您进行驾车导航", Toast.LENGTH_LONG).show();
                AMapNavi.getInstance(getActivity()).calculateDriveRoute(startList, endList, null, AMapNavi.DrivingDefault);
            }
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker != null && !marker.isInfoWindowShown()) {
            isHidden = false;
            marker.showInfoWindow();
        }
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
        _isChooseStart = false;
        if (_startMarker != null && _startMarker.equals(marker)) {
            LatLonPoint startPoint = new LatLonPoint(_startMarker.getPosition().latitude, _startMarker.getPosition().longitude);
            LatLonPoint destPoint = new LatLonPoint(locInfoBean.getLat() / 1000000.0, locInfoBean.getLon() / 1000000.0);
            chooseNav(startPoint, destPoint);
            _startMarker.remove();
            return;
        }
        if (marker != null && this.marker.equals(marker)) {
            isHidden = true;
        }
    }

    private double calculateDistance(double firstLat, double firstLon, double secondLat, double secondLon) {
        double d = getDistance(firstLat, firstLon, secondLat, secondLon);
        return d;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double difference = radLat1 - radLat2;
        double mdifference = rad(lng1) - rad(lng2);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(mdifference / 2), 2)));
        distance = distance * 6378.137f;
        distance = Math.round(distance * 10000) / 10.0f;

        return distance;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    @SuppressLint("DefaultLocale")
    private void checkToPlayDing(double d) {
        String dStr = String.format("%.2f米", d);
        dStr = dStr.substring(0, dStr.indexOf("."));
        showShortText("与车辆最后位置直线距离：" + dStr);
        if (d > 40.0) {
            if (_dingPlayerTimer != null) {
                _dingPlayerTimer.cancel();
            }

            if (_ding2PlayerTimer != null) {
                _ding2PlayerTimer.cancel();
            }
        } else if (d <= 40.0 && d > 10.0) //如果小于40米，播放声音"叮"
        {
            if (_ding2PlayerTimer != null) {
                _ding2PlayerTimer.cancel();
            }
            playDing(1000);
        } else if (d <= 10.0) {
            if (_dingPlayerTimer != null) {
                _dingPlayerTimer.cancel();
            }
            playDing2(3000);
        }
    }

    private void playDing(int intervalMs) {
        if (_dingPlayerTimer != null) {
            _dingPlayerTimer.cancel();
        }
        _dingPlayerTimer = new Timer();
        _dingPlayerTimerCount = 0;

        _dingPlayerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                playDingMsgPrompt();
                if (_dingPlayerTimerCount++ >= 20) {
                    _dingPlayerTimerCount = 0;
                    _dingPlayerTimer.cancel();
                }
            }
        }, 0, intervalMs);
    }

    private void playDing2(int intervalMs) {
        if (_ding2PlayerTimer != null) {
            _ding2PlayerTimer.cancel();
        }
        _ding2PlayerTimer = new Timer();
        _ding2PlayerTimerCount = 0;

        _ding2PlayerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                playDing2MsgPrompt();
                if (_ding2PlayerTimerCount++ >= 20) {
                    _ding2PlayerTimerCount = 0;
                    _ding2PlayerTimer.cancel();
                }
            }
        }, 0, intervalMs);
    }

    private void playDingMsgPrompt() {
        _dingPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _dingPlayer.stop();
                try {
                    _dingPlayer.prepare();
                } catch (IllegalStateException e) {
                    e.printStackTrace();

                    _dingPlayer.reset();
                    _dingPlayer = MediaPlayer.create(getActivity(), R.raw.ding_1);
                } catch (IOException e) {
                    e.printStackTrace();

                    _dingPlayer.reset();
                    _dingPlayer = MediaPlayer.create(getActivity(), R.raw.ding_1);
                } catch (Exception e) {
                    e.printStackTrace();

                    _dingPlayer.reset();
                    _dingPlayer = MediaPlayer.create(getActivity(), R.raw.ding_1);
                }
            }
        });

        _dingPlayer.start();
    }

    private void playDing2MsgPrompt() {
        _ding2Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _ding2Player.stop();
                try {
                    _ding2Player.prepare();
                } catch (IllegalStateException e) {
                    e.printStackTrace();

                    _ding2Player.reset();
                    _ding2Player = MediaPlayer.create(getActivity(), R.raw.ding_2);
                } catch (IOException e) {
                    e.printStackTrace();

                    _ding2Player.reset();
                    _ding2Player = MediaPlayer.create(getActivity(), R.raw.ding_2);
                } catch (Exception e) {
                    e.printStackTrace();

                    _ding2Player.reset();
                    _ding2Player = MediaPlayer.create(getActivity(), R.raw.ding_2);
                }
            }
        });

        _ding2Player.start();
    }

    public void stopNavi() {
        if (_dingPlayerTimer != null) {
            _dingPlayerTimer.cancel();
        }

        if (_ding2PlayerTimer != null) {
            _ding2PlayerTimer.cancel();
        }
        JCLocationManager.instance().stop();
        AMapNavi.getInstance(getActivity()).stopNavi();

    }

    //逆地理编码回调接口
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 0 && regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            addressTextView.setText(regeocodeAddress.getFormatAddress());
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    //切换回定位模式
    public void onEvent(ChangeLocationEvent event) {
        if (event != null && event.isChange() && hasTrack) {
            aMap.clear();
            requestDatas();
            //每30秒刷新一次电动车位置
            handler.postDelayed(this, 1000 * 30);
            hasTrack = false;
        }
    }

    public void onEvent(UserInfoBean bean) {
        if (AppConfig.userInfoBean != null && AppConfig.userInfoBean.getCarRecord() != null) {
            requestDatas();
        } else {
            hireCarLayout.setVisibility(View.VISIBLE);
            locationLayout.setVisibility(View.GONE);
        }
    }

    @Event(value = R.id.tv_send_validatecode)
    private void sendValidatecode(View v) {
        if (AppConfig.userInfoBean != null) {
            if (AppConfig.userInfoBean.getVip() == 2) {
                String carId = carIdText.getText().toString().trim();
                if (CommonUtils.strIsEmpty(carId)) {
                    T.showShort(getActivity(), "请输入车辆编号");
                    return;
                }
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("phone", AppConfig.userInfoBean.getPhone());
                paramsMap.put("carId", carId);
                RequestParams params = DRequestParamsUtils.getRequestParams(HttpConstants.sendHireCarCode(), paramsMap);
                DHttpUtils.post_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        ResponseBean<ValidateCodeBean> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<ValidateCodeBean>>() {
                        }.getType());
                        if (responseBean.getCode() == 1) {
                            validateCodeBean = responseBean.getData();
                            T.showShort(getActivity(), "验证码已发送至手机");
                            sendHandler = new Handler();
                            sendRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (minute > 0) {
                                        sendValidatecodeView.setEnabled(false);
                                        sendValidatecodeView.setText(minute + "s后可重发");
                                        minute--;
                                        handler.postDelayed(this, 1000);
                                    } else {
                                        sendValidatecodeView.setText("获取验证码");
                                        minute = 60;
                                        sendValidatecodeView.setEnabled(true);
                                    }
                                }
                            };
                            sendHandler.postDelayed(sendRunnable, 1000);
                        } else {
                            showShortText(responseBean.getErrmsg());
                        }
                    }
                });
            } else {
                CommonUtils.showCustomDialog3(getActivity(), "去办卡", "取消", "", "您还未办理租车卡，暂不能租车", new DSingleDialogCallback() {
                    @Override
                    public void onPositiveButtonClick(String editText) {
                        startActivity(new Intent(getActivity(), ManageCardActivity.class));
                    }
                });
            }
        } else {
            CommonUtils.showCustomDialog3(getActivity(), "去登录", "取消", "", "请先登录", new DSingleDialogCallback() {
                @Override
                public void onPositiveButtonClick(String editText) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            });
        }
    }

    @Event(value = R.id.btn_apply)
    private void apply(View view) {
        if (AppConfig.userInfoBean != null) {
            if (AppConfig.userInfoBean.getVip() == 2) {
                validateCode = validatecodeText.getText().toString().trim();
                if (CommonUtils.strIsEmpty(carIdText.getText().toString().trim())) {
                    T.showShort(getActivity(), "请输入车辆编号");
                    return;
                }
                if (CommonUtils.strIsEmpty(validateCode)) {
                    T.showShort(getActivity(), "请输入验证码");
                    return;
                }
                if (validateCodeBean != null
                        && new Date().getTime() < validateCodeBean.expireTime
                        && validateCodeBean.code.equals(validateCode)) {
                    Map<String, String> map = new HashMap<>();
                    map.put("userId", AppConfig.userInfoBean.getUserId());
                    map.put("carId", carIdText.getText().toString().trim());
                    RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.hireCar(), map);
                    DHttpUtils.post_String((BaseActivity) getActivity(), true, params, new DCommonCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            ResponseBean<CarRecordBean> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<CarRecordBean>>() {
                            }.getType());
                            if (responseBean.getCode() == 1) {
                                AppConfig.userInfoBean.setCarRecord(responseBean.getData());
                                EventBus.getDefault().post(AppConfig.userInfoBean);
                                carIdText.setText("");
                                validatecodeText.setText("");
                            } else {
                                T.showShort(getActivity(), responseBean.getErrmsg());
                            }
                        }
                    });
                } else {
                    T.showShort(getActivity(), "验证码不正确");
                }
            } else {
                CommonUtils.showCustomDialog3(getActivity(), "去办卡", "取消", "", "您还未办理租车卡，暂不能租车", new DSingleDialogCallback() {
                    @Override
                    public void onPositiveButtonClick(String editText) {
                        startActivity(new Intent(getActivity(), ManageCardActivity.class));
                    }
                });
            }
        } else {
            CommonUtils.showCustomDialog3(getActivity(), "去登录", "取消", "", "请先登录，再进行租车", new DSingleDialogCallback() {
                @Override
                public void onPositiveButtonClick(String editText) {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            });
        }
    }

    @Event(value = R.id.ll_around_car)
    private void aroundCar(View view) {
        startActivity(new Intent(getActivity(), AroundCarsActivity.class));
    }

    @Event(value = R.id.tv_back_car)
    private void backCar(View view) {
        //判断是否登录状态
        if (AppConfig.userInfoBean != null) {
            if (AppConfig.userInfoBean.getCarRecord() != null) {
                View backView = LayoutInflater.from(getActivity()).inflate(R.layout.view_back_car, null, false);
                final EditText validatecodeText = (EditText) backView.findViewById(R.id.et_validatecode);
                final Button validatecodeButton = (Button) backView.findViewById(R.id.btn_validatecode);
                Button submitButton = (Button) backView.findViewById(R.id.btn_submit);
                final CustomDialog dialog = CommonUtils.showCustomDialog1(getActivity(), "", backView);
                validatecodeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Map<String, String> paramsMap = new HashMap<>();
                        paramsMap.put("phone", AppConfig.userInfoBean.getPhone());
                        paramsMap.put("id", AppConfig.userInfoBean.getCarRecord().getId());
                        RequestParams params = DRequestParamsUtils.getRequestParams(HttpConstants.sendBackCarCode(), paramsMap);
                        DHttpUtils.post_String((HomeActivity) getActivity(), true, params, new DCommonCallback<String>() {
                            @Override
                            public void onSuccess(String result) {
                                ResponseBean<ValidateCodeBean> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<ValidateCodeBean>>() {
                                }.getType());
                                if (responseBean.getCode() == 1) {
                                    validateCodeBean = responseBean.getData();
                                    T.showShort(getActivity(), "验证码已发送至手机");
                                    backHandler = new Handler();
                                    backRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (minute > 0) {
                                                validatecodeButton.setEnabled(false);
                                                validatecodeButton.setText(minute + "s后可重发");
                                                minute--;
                                                handler.postDelayed(this, 1000);
                                            } else {
                                                validatecodeButton.setText("获取验证码");
                                                minute = 60;
                                                validatecodeButton.setEnabled(true);
                                            }
                                        }
                                    };
                                    backHandler.postDelayed(backRunnable, 1000);
                                } else {
                                    showShortText(responseBean.getErrmsg());
                                }
                            }
                        });
                    }
                });
                submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String validatecode = validatecodeText.getText().toString().trim();
                        if (CommonUtils.strIsEmpty(validatecode)) {
                            T.showShort(getActivity(), "请输入验证码");
                            return;
                        }
                        if (validateCodeBean != null
                                && new Date().getTime() < validateCodeBean.expireTime
                                && validateCodeBean.code.equals(validatecode)) {
                            dialog.cancel();
                            DMethods.backCar(getActivity(), "1", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    T.showShort(getActivity(), "还车成功");
                                    topBarView.setRightTextView("");
                                    topBarView.setCenterTextView("申请用车");
                                    handler.removeCallbacks(HireCarFragment.this); //停止刷新
                                    if (countHandler != null) {
                                        countHandler.removeCallbacks(countRunnable);
                                    }
                                }
                            });
                        } else {
                            T.showShort(getActivity(), "验证码不正确");
                        }
                    }
                });
            } else {
                T.showShort(getActivity(), "您没有租车");
            }
        } else {
            T.showShort(getActivity(), "请您先登录账号，再进行还车");
        }
    }

    @Event(value = R.id.tv_lock)
    private void lock(View view) {
        if (locInfoBean.getLock().equals("1")) {
            RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getUnLockBikeUrl("0"));
            DHttpUtils.get_String((HomeActivity) getActivity(), false, params, new DCommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    ResponseBean<String> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<String>>() {
                    }.getType());
                    if (responseBean != null) {
                        AppConfig.isExecuteLock = 0;
                        showShortText(responseBean.getErrmsg());
                    }
                }
            });
        } else {
            RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.getlockBikeUrl("0"));
            DHttpUtils.get_String((HomeActivity) getActivity(), false, params, new DCommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    ResponseBean<String> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<String>>() {
                    }.getType());
                    if (responseBean != null) {
                        AppConfig.isExecuteLock = 1;
                        showShortText(responseBean.getErrmsg());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapNavi.destroy();
        mapView.onDestroy();
        handler.removeCallbacks(this); //停止刷新
        if (sendHandler != null) {
            sendHandler.removeCallbacks(sendRunnable);
        }
        if (backHandler != null) {
            backHandler.removeCallbacks(backRunnable);
        }
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!CommonUtils.strIsEmpty(carIdText.getText().toString().trim())) {
            carIdView.setImageResource(R.mipmap.icon_ebike_active);
            carIdLine.setBackgroundColor(getResources().getColor(R.color.main_tone));
            carIdText.setTextColor(getResources().getColor(R.color.main_tone));
        } else {
            carIdView.setImageResource(R.mipmap.icon_ebike);
            carIdLine.setBackgroundColor(getResources().getColor(R.color.font_gray));
        }
        if (!CommonUtils.strIsEmpty(validatecodeText.getText().toString().trim())) {
            validatecodeView.setImageResource(R.mipmap.icon_phone_active);
            validatecodeLine.setBackgroundColor(getResources().getColor(R.color.main_tone));
            validatecodeText.setTextColor(getResources().getColor(R.color.main_tone));
        } else {
            validatecodeView.setImageResource(R.mipmap.icon_phone);
            validatecodeLine.setBackgroundColor(getResources().getColor(R.color.font_gray));
        }
    }
}
