package com.xiaomizuche.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomizuche.R;
import com.xiaomizuche.adapter.SchoolAdapter;
import com.xiaomizuche.base.BaseActivity;
import com.xiaomizuche.bean.LocationJson;
import com.xiaomizuche.bean.ResponseBean;
import com.xiaomizuche.bean.SchoolBean;
import com.xiaomizuche.bean.UserInfoBean;
import com.xiaomizuche.callback.DCommonCallback;
import com.xiaomizuche.constants.AppConfig;
import com.xiaomizuche.db.ProvinceInfoDao;
import com.xiaomizuche.http.DHttpUtils;
import com.xiaomizuche.http.DRequestParamsUtils;
import com.xiaomizuche.http.HttpConstants;
import com.xiaomizuche.utils.CommonUtils;
import com.xiaomizuche.utils.IDCard;
import com.xiaomizuche.utils.T;
import com.xiaomizuche.view.CustomDialog;
import com.xiaomizuche.view.wheel.AddressThreeWheelViewDialog;

import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddUserInfoActivity extends BaseActivity {

    @ViewInject(R.id.et_name)
    EditText nameText;
    @ViewInject(R.id.v_line_name)
    View nameLine;
    @ViewInject(R.id.ll_sex)
    LinearLayout sexLayout;
    @ViewInject(R.id.tv_sex)
    TextView sexView;
    @ViewInject(R.id.et_id_card)
    EditText idCardText;
    @ViewInject(R.id.v_line_id_card)
    View idCardLine;
    @ViewInject(R.id.tv_area)
    TextView areaView;
    @ViewInject(R.id.tv_user_type)
    TextView userTypeView;
    @ViewInject(R.id.ll_school)
    LinearLayout schoolLayout;
    @ViewInject(R.id.tv_school)
    TextView schoolView;
    @ViewInject(R.id.v_line_school)
    View schoolLine;
    @ViewInject(R.id.ll_address)
    LinearLayout addressLayout;
    @ViewInject(R.id.et_address)
    EditText addressText;
    @ViewInject(R.id.v_line_address)
    View addressLine;
    @ViewInject(R.id.btn_save)
    Button saveButton;

    private String name;
    private String sex;
    private String idCard;
    private String userType;
    private String school;
    private String address;

    private AddressThreeWheelViewDialog dialog;
    private List<LocationJson> mProvinceList;
    private ProvinceInfoDao provinceDao;
    private String province;
    private String city;
    private String county;

    private List<SchoolBean> schoolList;
    private SchoolBean schoolBean;

    @Override
    public void loadXml() {
        setContentView(R.layout.activity_add_user_info);
    }

    @Override
    public void getIntentData(Bundle savedInstanceState) {

    }

    @Override
    public void init() {
        dialog = new AddressThreeWheelViewDialog(this);
        provinceDao = new ProvinceInfoDao(this);
        mProvinceList = provinceDao.queryAll();
    }

    @Override
    public void setListener() {

    }

    @Event(value = R.id.ll_sex)
    private void sex(View view) {
        chooseSex();
    }

    @Event(value = R.id.ll_school)
    private void school(View view) {
        chooseSchool();
    }

    @Event(value = R.id.ll_area)
    private void area(View view) {
        dialog.setData(mProvinceList);
        dialog.show(new AddressThreeWheelViewDialog.ConfirmAction() {
            @Override
            public void doAction(LocationJson root, LocationJson child, LocationJson child2) {
                province = root.getName();
                city = child.getName();
                county = child2.getName();
                areaView.setText(province + "-" + city + "-" + county);
                Map<String, String> map = new HashMap<>();
                map.put("province", province);
                map.put("city", city);
                map.put("area", county);
                RequestParams params = DRequestParamsUtils.getRequestParams(HttpConstants.getSchools(), map);
                DHttpUtils.post_String(AddUserInfoActivity.this, false, params, new DCommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        ResponseBean<List<SchoolBean>> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<List<SchoolBean>>>() {
                        }.getType());
                        if (responseBean.getCode() == 1) {
                            schoolList = responseBean.getData();
                        } else {
                            showShortText(responseBean.getErrmsg());
                        }
                    }
                });
            }
        });
    }

    @Event(value = R.id.ll_user_type)
    private void userType(View view) {
        chooseUserType();
    }

    @Event(value = R.id.btn_save)
    private void save(View view) {
        if (CommonUtils.strIsEmpty(nameText.getText().toString().trim())) {
            T.showShort(this, "请输入姓名");
            return;
        }
        if (CommonUtils.strIsEmpty(idCardText.getText().toString().trim())) {
            T.showShort(this, "请输入身份证号码");
            return;
        }
        IDCard idCard = new IDCard();
        String errorInfo = idCard.IDCardValidate(idCardText.getText().toString().trim().toLowerCase());
        if (!CommonUtils.strIsEmpty(errorInfo)) {
            T.showShort(this, "身份证号码不合法");
            return;
        }
        if (CommonUtils.strIsEmpty(areaView.getText().toString())) {
            T.showShort(this, "请选择省市区");
            return;
        }
        if (CommonUtils.strIsEmpty(userType)) {
            T.showShort(this, "请选择用户类型");
            return;
        }
        if (userType.equals("2") && CommonUtils.strIsEmpty(addressText.getText().toString().trim())) {
            T.showShort(this, "请输入详细地址");
            return;
        }
        if (userType.equals("1") && CommonUtils.strIsEmpty(schoolView.getText().toString())) {
            T.showShort(this, "请选择学校");
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("userId", AppConfig.userInfoBean.getUserId());
        map.put("userName", nameText.getText().toString().trim());
        map.put("sex", sex);
        map.put("idNum", idCardText.getText().toString().trim());
        map.put("userType", userType);
        map.put("province", province);
        map.put("city", city);
        map.put("area", county);
        if (userType.equals("1")) {
            map.put("schoolId", schoolBean.getId());
        } else if (userType.equals("2")) {
            map.put("address", addressText.getText().toString().trim());
        }
        RequestParams params = DRequestParamsUtils.getRequestParams_Header(HttpConstants.perfectUserData(), map);
        DHttpUtils.post_String(this, true, params, new DCommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                ResponseBean<UserInfoBean> responseBean = new Gson().fromJson(result, new TypeToken<ResponseBean<UserInfoBean>>() {
                }.getType());
                if (responseBean.getCode() == 1) {
                    AppConfig.userInfoBean = responseBean.getData();
                    AddUserInfoActivity.this.finish();
                } else {
                    showShortText(responseBean.getErrmsg());
                }
            }
        });
    }

    @Override
    public void setData() {

    }

    private void chooseSex() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_sex, null, false);
        TextView manView = (TextView) view.findViewById(R.id.tv_man);
        TextView womanView = (TextView) view.findViewById(R.id.tv_woman);
        TextView otherView = (TextView) view.findViewById(R.id.tv_other);
        Drawable drawable = getResources().getDrawable(R.mipmap.icon_sel);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        if (!CommonUtils.strIsEmpty(sex)) {
            if ("0".equals(sex)) {
                manView.setCompoundDrawables(null, null, drawable, null);
            } else if ("1".equals(sex)) {
                womanView.setCompoundDrawables(null, null, drawable, null);
            } else if ("2".equals(sex)) {
                otherView.setCompoundDrawables(null, null, drawable, null);
            }
        }
        final CustomDialog dialog = CommonUtils.showCustomDialog1(this, "选择性别", view);
        manView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                sex = "0";
                sexView.setText("男");
            }
        });
        womanView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                sex = "1";
                sexView.setText("女");
            }
        });
        otherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                sex = "2";
                sexView.setText("保密");
            }
        });
    }

    private void chooseUserType() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_user_type, null, false);
        TextView schoolUserView = (TextView) view.findViewById(R.id.tv_school_user);
        TextView commonUserView = (TextView) view.findViewById(R.id.tv_common_user);
        Drawable drawable = getResources().getDrawable(R.mipmap.icon_sel);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        if (!CommonUtils.strIsEmpty(userType)) {
            if ("1".equals(userType)) {
                schoolUserView.setCompoundDrawables(null, null, drawable, null);
            } else if ("2".equals(userType)) {
                commonUserView.setCompoundDrawables(null, null, drawable, null);
            }
        }
        final CustomDialog dialog = CommonUtils.showCustomDialog1(this, "选择用户类型", view);
        schoolUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                userType = "1";
                userTypeView.setText("学校用户");
                schoolLayout.setVisibility(View.VISIBLE);
                addressLayout.setVisibility(View.GONE);
            }
        });
        commonUserView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                userType = "2";
                userTypeView.setText("普通用户");
                schoolLayout.setVisibility(View.GONE);
                addressLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void chooseSchool() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_school, null, false);
        ListView listView = (ListView) view.findViewById(R.id.list_view);
        SchoolAdapter adapter = new SchoolAdapter(this);
        if (schoolBean != null) {
            adapter.setSchoolId(schoolBean.getId());
        }
        if (schoolList != null && schoolList.size() > 0) {
            adapter.setList(schoolList);
        }
        listView.setAdapter(adapter);
        final CustomDialog dialog = CommonUtils.showCustomDialog1(this, "选择学校", view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();
                schoolBean = (SchoolBean) parent.getItemAtPosition(position);
                schoolView.setText(schoolBean.getName());
            }
        });
    }
}
