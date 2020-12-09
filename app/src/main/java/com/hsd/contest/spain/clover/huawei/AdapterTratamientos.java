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

import static com.hsd.contest.spain.clover.huawei.SetTimeActivity.areAllFalse;
import static com.hsd.contest.spain.clover.huawei.SetTimeActivity.areAllTrue;

public class AdapterTratamientos extends RecyclerView.Adapter<AdapterTratamientos.ViewHolderTratamientos> {

    ArrayList<Dosis> listDatos;
    CardView cards;
    private OnCardListener mOnCardListener;

    public AdapterTratamientos(ArrayList<Dosis> listDatos, OnCardListener onCardListener) {
        this.listDatos = listDatos;
        this.mOnCardListener = onCardListener;
    }

    @Override
    public ViewHolderTratamientos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_tratamientos, null, false);
        cards = view.findViewById(R.id.card_tratamiento);
        return new ViewHolderTratamientos(view, mOnCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderTratamientos holder, final int position) {
        // Construimos un String que informe al usuario de la frecuencia de sus dosis.
        listDatos.get(position).getmFreqDias();
        StringBuilder sb = new StringBuilder("Cada ");
        String res;
        if (areAllTrue(listDatos.get(position).getmFreqDias())) {
            // TODO: Cada día a las XX:XX, YY:YY...
            sb.append("día a las");
            res = concatHoras(sb, position);
        }
        else if (areAllFalse(listDatos.get(position).getmFreqDias())) {
            // TODO: Cada X días/semanas a las XX:XX, YY:YY...
            listDatos.get(position).getmFreqNum();
            sb.append(listDatos.get(position).getmFreqNum());

            if (listDatos.get(position).getmFreqTime() == 0) {
                // Días
                sb.append(" días a las ");
            }
            else {
                // Semanas
                sb.append(" semanas a las ");
            }
            res = concatHoras(sb, position);
        }
        else {
            // TODO: L/M/X/J/V/S/D... a las XX:XX, YY:YY...
            for (int i = 0; i < listDatos.get(position).getmFreqDias().length; i++) {
                if (listDatos.get(position).getmFreqDias()[i]) {
                    switch (i) {
                        case 1:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("lunes");
                            break;
                        case 2:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("martes");
                            break;
                        case 3:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("miércoles");
                            break;
                        case 4:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("jueves");
                            break;
                        case 5:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("viernes");
                            break;
                        case 6:
                            if (listDatos.get(position).getmFreqDias()[i])
                                sb.append("sábado");
                            break;
                    }

                    // FIXME: Arreglar en la próxima versión.
                    if (i != 0 && i < listDatos.get(position).getmFreqDias().length - 1)
                        sb.append(", ");
                }
            }

            // La lista tiene el domingo en la posición 0, pero nosotros lo queremos poner al final al mostrar los días de tratamiento al usuario.
            if (listDatos.get(position).getmFreqDias()[0])
                sb.append("domingo");

            sb.append(" a las ");
            res = concatHoras(sb, position);
        }

        holder.asignarDatos(listDatos.get(position).getmNombre(), res);
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

    public static class ViewHolderTratamientos extends RecyclerView.ViewHolder {
        TextView nombre;
        TextView info;
        ImageButton more;
        OnCardListener onCardListener;

        public ViewHolderTratamientos(@NonNull final View itemView, OnCardListener onCardListener) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre_tratamiento);
            info = itemView.findViewById(R.id.info_tratamiento);
            more = itemView.findViewById(R.id.delete_tratamiento);
            this.onCardListener = onCardListener;
        }

        public void asignarDatos(final String str_nombre, final String str_info) {
            nombre.setText(str_nombre);
            info.setText(str_info);
        }
    }

    public interface OnCardListener {
        void onPopupMenuClick(View view, int position);
    }

    private String concatHoras(StringBuilder sb, int position) {
        for (int i = 0; i < listDatos.get(position).getHoras().size(); i++) {
            sb.append(listDatos.get(position).getHoras().get(i));
            if (i != listDatos.get(position).getHoras().size() - 1 && listDatos.get(position).getHoras().size() != 1) {
                sb.append(", ");
            }
        }
        sb.append(".");
        return sb.toString();
    }
}