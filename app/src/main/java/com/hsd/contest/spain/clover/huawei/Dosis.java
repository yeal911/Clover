package com.hsd.contest.spain.clover.huawei;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Dosis implements Parcelable {
    private String mNombre;
    private int mPastillasBlister;
    private ArrayList<String> mHoras;
    private double mPastillasPorDosis;
    
    private boolean[] mFreqDias;

    // X días/semanas
    private int mFreqNum;
    private int mFreqTime;
    
    public Dosis() {}

    public Dosis(String mNombre, int mPastillasBlister, ArrayList<String> mHoras, double mPastillasPorDosis, int mFreqNum, int mFreqTime, boolean[] mFreqDias) {
        this.mNombre = mNombre;
        this.mPastillasBlister = mPastillasBlister;
        this.mHoras = mHoras;
        this.mPastillasPorDosis = mPastillasPorDosis;
        this.mFreqNum = mFreqNum;
        this.mFreqTime = mFreqTime;
        this.mFreqDias = mFreqDias;
    }

    protected Dosis(Parcel in) {
        mNombre = in.readString();
        mPastillasBlister = in.readInt();
        mHoras = in.createStringArrayList();
        mPastillasPorDosis = in.readDouble();
        mFreqDias = in.createBooleanArray();
        mFreqNum = in.readInt();
        mFreqTime = in.readInt();
    }

    public static final Creator<Dosis> CREATOR = new Creator<Dosis>() {
        @Override
        public Dosis createFromParcel(Parcel in) {
            return new Dosis(in);
        }

        @Override
        public Dosis[] newArray(int size) {
            return new Dosis[size];
        }
    };

    public String getmNombre() {
        return mNombre;
    }

    public int getmFreqNum() {
        return mFreqNum;
    }

    public int getmFreqTime() {
        return mFreqTime;
    }

    public boolean[] getmFreqDias() {
        return mFreqDias;
    }

    public ArrayList<String> getHoras() {
        return mHoras;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mNombre);
        parcel.writeInt(mPastillasBlister);
        parcel.writeStringList(mHoras);
        parcel.writeDouble(mPastillasPorDosis);
        parcel.writeBooleanArray(mFreqDias);
        parcel.writeInt(mFreqNum);
        parcel.writeInt(mFreqTime);
    }

    // Obligatorio para que Jackson haga su trabajo.
    public void setmNombre(String mNombre) {
        this.mNombre = mNombre;
    }

    public int getmPastillasBlister() {
        return mPastillasBlister;
    }

    public void setmPastillasBlister(int mPastillasBlister) {
        this.mPastillasBlister = mPastillasBlister;
    }

    public ArrayList<String> getmHoras() {
        return mHoras;
    }

    public void setmHoras(ArrayList<String> mHoras) {
        this.mHoras = mHoras;
    }

    public double getmPastillasPorDosis() {
        return mPastillasPorDosis;
    }

    public void setmPastillasPorDosis(double mPastillasPorDosis) {
        this.mPastillasPorDosis = mPastillasPorDosis;
    }

    public void setmFreqDias(boolean[] mFreqDias) {
        this.mFreqDias = mFreqDias;
    }

    public void setmFreqNum(int mFreqNum) {
        this.mFreqNum = mFreqNum;
    }

    public void setmFreqTime(int mFreqTime) {
        this.mFreqTime = mFreqTime;
    }
}
