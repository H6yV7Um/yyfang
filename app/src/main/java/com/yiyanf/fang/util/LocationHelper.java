package com.yiyanf.fang.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import com.yiyanf.fang.FangConstants;
import com.yiyanf.fang.R;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * 位置服务类
 */
public class LocationHelper {
    private static String TAG = "LocationHelper";

    private static LocationListener mLocationListener;

    static public boolean checkLocationPermission(final @NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FangConstants.LOCATION_PERMISSION_REQ_CODE);
                return false;
            }
        }

        return true;
    }

    static private String getAddressFromLocation(final @NonNull Activity activity, Location location) {
        Geocoder geocoder = new Geocoder(activity, Locale.CHINESE);

        try {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d(TAG, "getAddressFromLocation->lat:" + latitude + ", long:" + longitude);
            List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
            if (!list.isEmpty()) {
                //返回当前位置，精度可调
                Address address = list.get(0);
                String sAddress = "";

                String adminArea = address.getAdminArea();
                String locality = address.getLocality();
                String subLocation = address.getSubLocality();
                String detail = address.getFeatureName();
                // getAdminArea 省份  locality 城市   getSubLocality 区县  getFeatureName 周边地址
                LogUtil.v("hition==","省份："+adminArea+"，城市："+locality+"，区："+subLocation+"，详细："+detail);
                if(!TextUtils.isEmpty(address.getLocality())) {
                    sAddress = adminArea+";"+locality;
                    /*if(!TextUtils.isEmpty(detail)) {
                        sAddress = address.getLocality();
                    } else {
                        sAddress = address.getLocality();
                    }*/
                }
                return sAddress;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "定位失败";
    }

    static public boolean getMyLocation(final @NonNull Activity activity, final @NonNull OnLocationListener listener) {
        final LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setCancelable(false);
            dialog.setMessage("尚未开启位置定位服务");
            dialog.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
            return false;
        }

        if (!checkLocationPermission(activity)) {
            return true;
        }

        Criteria criteria=new Criteria();
        // 位置解析的精度。Criteria.Accuracy_Fine 精确模式，Criteria.Accuracy_Coarse 模糊模式
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 是否提供海拔高度信息
        criteria.setAltitudeRequired(false);
        // 是否提供方向信息
        criteria.setBearingRequired(false);
        // 是否允许运行商计费。
        criteria.setCostAllowed(false);
        // 电池消耗，无、低、中、高，参
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        Location curLoc = locationManager.getLastKnownLocation(provider);
//        Location curLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (null == curLoc) {
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    String strAddr = getAddressFromLocation(activity, location);
                    if (TextUtils.isEmpty(strAddr)) {
                        listener.onLocationChanged(-1, 0, 0, strAddr);
                    } else {
                        listener.onLocationChanged(0, location.getLatitude(), location.getLongitude(), strAddr);
                    }
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    locationManager.removeUpdates(this);
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8000, 0, mLocationListener);
        } else {
            String strAddr = getAddressFromLocation(activity, curLoc);
            if (TextUtils.isEmpty(strAddr)) {
                listener.onLocationChanged(-1, 0, 0, strAddr);
            } else {
                listener.onLocationChanged(0, curLoc.getLatitude(), curLoc.getLongitude(), strAddr);
            }
        }
        return true;
    }

    public interface OnLocationListener {
        void onLocationChanged(int code, double lat1, double long1, String location);
    }
}
