package com.hsd.contest.spain.clover.huawei;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdapterHoras extends RecyclerView.Adapter<AdapterHoras.ViewHolderHoras> {

    ArrayList<String> listDatos;
    CardView cards;
    private OnCardListener mOnCardListener;

    public AdapterHoras(ArrayList<String> listDatos, OnCardListener onCardListener) {
        this.listDatos = listDatos;
        this.mOnCardListener = onCardListener;
    }

    @Override
    public ViewHolderHoras onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_horas, null, false);
        cards = view.findViewById(R.id.card_hora);
        return new ViewHolderHoras(view, mOnCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderHoras holder, final int position) {
        holder.asignarDatos(listDatos.get(position));
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCardListener.onPopupMenuClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listDatos.size();
    }

    public static class ViewHolderHoras extends RecyclerView.ViewHolder {
        TextView nombre;
        ImageButton more;
        OnCardListener onCardListener;

        public ViewHolderHoras(@NonNull final View itemView, OnCardListener onCardListener) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre_hora);
            more = itemView.findViewById(R.id.delete_hora);
            this.onCardListener = onCardListener;
        }

        public void asignarDatos(final String m) {
            nombre.setText(m);
        }
    }

    public interface OnCardListener {
        void onPopupMenuClick(View view, int position);
    }
}