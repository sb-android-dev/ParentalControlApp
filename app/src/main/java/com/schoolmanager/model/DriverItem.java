package com.schoolmanager.model;

import com.google.android.gms.maps.model.LatLng;

public class DriverItem {

    private int driverId;
    private String driverName;
    private String vehicleNo;
    private String phoneNo;
    private String driverAddress;
    private LatLng driverLocation;
    private String driverImage;
    private boolean isSelected;

    public DriverItem() {
    }

    public DriverItem(int driverId, String driverName, String vehicleNo, String phoneNo,
                      String driverAddress, LatLng driverLocation, String driverImage, boolean isSelected) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.vehicleNo = vehicleNo;
        this.phoneNo = phoneNo;
        this.driverAddress = driverAddress;
        this.driverLocation = driverLocation;
        this.driverImage = driverImage;
        this.isSelected = isSelected;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getDriverAddress() {
        return driverAddress;
    }

    public void setDriverAddress(String driverAddress) {
        this.driverAddress = driverAddress;
    }

    public LatLng getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(LatLng driverLocation) {
        this.driverLocation = driverLocation;
    }

    public String getDriverImage() {
        return driverImage;
    }

    public void setDriverImage(String driverImage) {
        this.driverImage = driverImage;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
