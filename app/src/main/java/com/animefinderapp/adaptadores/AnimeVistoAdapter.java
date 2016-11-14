package com.animefinderapp.adaptadores;

import java.util.ArrayList;

import com.animefinderapp.R;
import com.animefinderapp.actividades.AnimeActivity;
import com.animefinderapp.baseDatos.AnimeDataSource;
import com.animefinderapp.entidades.AnimeFavorito;
import com.animefinderapp.utilidad.CropSquareTransformationGlide;
import com.animefinderapp.utilidad.ServidorUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AnimeVistoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Context context;
    private ArrayList<AnimeFavorito> listaAnimes;
    private String server;
    private int resourse;

    public AnimeVistoAdapter(Context context, int resource, ArrayList<AnimeFavorito> listaAnimes, String server) {
        this.context = context;
        this.resourse = resource;
        this.listaAnimes = listaAnimes;
        this.server = server;
    }

    @Override
    public int getItemCount() {
        return listaAnimes.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view;
        if (i == TYPE_ITEM) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(resourse, viewGroup, false);
            return new ViewHolderItem(view);
        } else if (i == TYPE_HEADER) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            return new ViewHolderHeader(view);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int posicion) {
        if (viewHolder instanceof ViewHolderHeader) {
            ((ViewHolderHeader) viewHolder).numero.setText("Total: " + listaAnimes.size());
        } else if (viewHolder instanceof ViewHolderItem) {
            final AnimeFavorito animeFavorito = listaAnimes.get(posicion - 1);
            Glide.with(context).load(animeFavorito.getAnime().getImagen())
                    .diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.loadimage)
                    .bitmapTransform(new CropSquareTransformationGlide(context)).into(((ViewHolderItem) viewHolder).imagen);
            ((ViewHolderItem) viewHolder).visto.setVisibility(ImageView.GONE);
            ((ViewHolderItem) viewHolder).titulo.setText(animeFavorito.getAnime().getTitulo().trim());
            ((ViewHolderItem) viewHolder).descripcion.setText(animeFavorito.getAnime().getDescripcion().trim());
            viewHolder.itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ServidorUtil.verificaConexion(context)) {
                        Intent i = new Intent(context, AnimeActivity.class);
                        i.putExtra("url", animeFavorito.getAnime().getUrl());
                        context.startActivity(i);
                    } else {
                        Snackbar.make(v, "Sin conexion", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    AnimeDataSource.eliminarAnimeVisto(animeFavorito, server, context);
                    listaAnimes.remove(animeFavorito);
                    Snackbar.make(v, "Anime Eliminado", Snackbar.LENGTH_SHORT).show();
                    notifyItemRemoved(posicion);
                    return false;
                }
            });
        }
    }

    public void addAll(ArrayList<AnimeFavorito> lista) {
        listaAnimes.addAll(lista);
        notifyDataSetChanged();
    }

    public void removeAll() {
        listaAnimes.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public static class ViewHolderItem extends RecyclerView.ViewHolder {
        TextView titulo;
        TextView descripcion;
        ImageView imagen;
        ImageView visto;

        ViewHolderItem(View itemView) {
            super(itemView);
            imagen = (ImageView) itemView.findViewById(R.id.listaAnimeImagen);
            visto = (ImageView) itemView.findViewById(R.id.isVisto);
            titulo = (TextView) itemView.findViewById(R.id.listaAnimeTitulo);
            descripcion = (TextView) itemView.findViewById(R.id.listaAnimeDescripcion);
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        TextView numero;

        ViewHolderHeader(View itemView) {
            super(itemView);
            numero = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}
