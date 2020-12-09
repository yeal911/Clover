package com.hsd.contest.spain.clover.huawei;

import java.util.ArrayList;

public class Usuario
{
    private String mNombre;
    private ArrayList<Dosis> mDosis;

    // Obligatorio para restaurar el JSON.
    public Usuario() {}

    public Usuario(String mNombre, ArrayList<Dosis> mDosis) {}

    public Usuario(String mNombre) {
        this.mNombre = mNombre;
        mDosis = new ArrayList<>();
    }

    // Obligatorio para restaurar el JSON.
    public void setNombre(String mNombre) {
        this.mNombre = mNombre;
    }

    public String getNombre() {
        return mNombre;
    }

    // Obligatorio para restaurar el JSON.
    public void setDosis(ArrayList<Dosis> mDosis) {
        this.mDosis = mDosis;
    }

    public ArrayList<Dosis> getDosis() {
        return mDosis;
    }

    public void addDosis(Dosis dosis) {
        mDosis.add(mDosis.size(), dosis);
    }

    // TODO: DeleteDosis, EditDosis...
}
