package com.animefinderapp.adaptadores;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.Capitulo;
import com.animefinderapp.utilidad.ServidorUtil;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class CapitulosAdapter extends RecyclerView.Adapter<CapitulosAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Capitulo> capitulos;
    private int resourse;
    private String server;

    public CapitulosAdapter(Context context, int resource, ArrayList<Capitulo> objects, String server) {
        this.context = context;
        this.resourse = resource;
        this.capitulos = objects;
        this.server = server;
    }

    @Override
    public int getItemCount() {
        return capitulos.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(resourse, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int posicion) {
        final Capitulo capitulo = capitulos.get(posicion);
        viewHolder.titulo.setText(capitulo.getTitulo());
        if (AnimeDataSource.getAllCapitulosVistos(server, capitulo.getAnime().getUrl(), context).contains(capitulos.get(posicion).getUrl())) {
            viewHolder.titulo.setTextColor(context.getResources().getColor(R.color.colorAccent));

        } else {
            viewHolder.titulo.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        viewHolder.itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (ServidorUtil.verificaConexion(context)) {
                        if (!AnimeDataSource.getAllCapitulosVistos(server, capitulo.getAnime().getUrl(), context)
                                .contains(capitulo.getUrl())) {
                            AnimeDataSource.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl(), context);
                            AnimeDataSource.agregarHistorial(capitulo, server, context);
                            notifyDataSetChanged();
                        }
                        ServidorUtil.buscarVideos(capitulo, "ver", server, context);
                    } else {
                        Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("error", e.getLocalizedMessage());
                }
            }
        });
        viewHolder.boton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                PopupMenu popup = new PopupMenu(context, v);

                // This activity implements OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.marcar_desmarcar:
                                Log.i("capitulo", capitulo.getUrl());
                                if (AnimeDataSource.getAllCapitulosVistos(server, capitulo.getAnime().getUrl(), context)
                                        .contains(capitulo.getUrl())) {
                                    AnimeDataSource.eliminarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl(), context);
                                    notifyItemChanged(posicion);
                                } else {
                                    AnimeDataSource.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl(), context);
                                    AnimeDataSource.agregarHistorial(capitulo, server, context);
                                    notifyItemChanged(posicion);
                                }
                                return true;
                            case R.id.descargar:
                                if (ServidorUtil.verificaConexion(context)) {
                                    AnimeDataSource.agregarHistorial(capitulo, server, context);
                                    AnimeDataSource.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl(), context);
                                    ServidorUtil.buscarVideos(capitulo, "descargar", server, context);
                                    notifyItemChanged(posicion);
                                } else {
                                    Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                                }

                                return true;
                            case R.id.ver:
                                if (ServidorUtil.verificaConexion(context)) {
                                    AnimeDataSource.agregarCapitulo(server, capitulo.getAnime().getUrl(), capitulo.getUrl(), context);
                                    ServidorUtil.buscarVideos(capitulo, "ver", server, context);
                                    notifyItemChanged(posicion);
                                } else {
                                    Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                                }
                                return true;
                        }
                        return false;
                    }
                });
                popup.inflate(R.menu.acciones);
                popup.show();
            }
        });
    }

    public void addAll(ArrayList<Capitulo> lista) {
        int first = capitulos.size();
        capitulos.addAll(lista);
        notifyItemRangeInserted(first, lista.size());
    }

    public void removeAll() {
        int last = capitulos.size();
        capitulos.clear();
        notifyItemRangeRemoved(0, last);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo;
        Button boton;

        ViewHolder(View itemView) {
            super(itemView);
            titulo = (TextView) itemView.findViewById(R.id.capituloTitulo);
            boton = (Button) itemView.findViewById(R.id.buttonAccion);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
